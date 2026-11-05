package ph.marker.glassplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import android.media.AudioManager;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import org.json.JSONObject;
import java.net.*;
import java.util.*;
import java.io.*;

public class MainActivity extends Activity implements RemoteServer.Receiver {
 private ExoPlayer player;
 private PlayerView playerView;
 private WebView web;
 private TextView status;
 private RemoteServer server;
 private boolean youtube=false;
 private String pairing="Starting receiver…";
 private final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
 private boolean closed=false;
 private final Runnable poll=new Runnable() { public void run() {
  if(closed) return;
  if(youtube) web.evaluateJavascript("JSON.stringify(window.glassStatus || 'Loading YouTube…')",value->{
   if(!closed && youtube) try { status.setText(new org.json.JSONTokener(value).nextValue().toString()); } catch(Exception ignored) {}
  });
  else if(player.getMediaItemCount()>0) status.setText((player.isPlaying()?"PLAYING ":"PAUSED ")+format(player.getCurrentPosition())+" / "+format(player.getDuration()));
  handler.postDelayed(this,1000);
 }};
 @Override public void onCreate(Bundle b) {
  super.onCreate(b);
  getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  getWindow().getDecorView().setSystemUiVisibility(5894);
  LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.BLACK);
  LinearLayout bar=new LinearLayout(this);
  status=new TextView(this); status.setTextColor(Color.GREEN); status.setTextSize(14); status.setPadding(8,8,8,8); status.setText("GLASSPLAYER • Ready");
  bar.addView(status,new LinearLayout.LayoutParams(0,-2,1));
  Button connect=new Button(this); connect.setText("Pair iPhone"); connect.setOnClickListener(v->showPairing()); bar.addView(connect);
  Button open=new Button(this); open.setText("Open URL"); open.setOnClickListener(v->input()); bar.addView(open); root.addView(bar);
  FrameLayout stage=new FrameLayout(this); root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
  player=new ExoPlayer.Builder(this).build(); playerView=new PlayerView(this); playerView.setPlayer(player); stage.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
  web=new WebView(this); web.setBackgroundColor(Color.BLACK); web.getSettings().setJavaScriptEnabled(true); web.getSettings().setDomStorageEnabled(true); web.getSettings().setMediaPlaybackRequiresUserGesture(false); web.getSettings().setAllowFileAccess(false); web.getSettings().setAllowContentAccess(false);
  web.setWebChromeClient(new WebChromeClient());
  web.setWebViewClient(new WebViewClient() { @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request) { return request.isForMainFrame(); } });
  stage.addView(web,new FrameLayout.LayoutParams(-1,-1)); web.setVisibility(View.GONE);
  setContentView(root);
  player.addListener(new Player.Listener(){ @Override public void onPlayerError(PlaybackException error) { status.setText("Playback failed: "+error.getErrorCodeName()+" • check media URL/network"); handler.removeCallbacks(poll); } });
  playerView.setUseController(false);
  playerView.setOnClickListener(v->command("toggle"));
  final float[] touchStart=new float[2];
  playerView.setOnTouchListener((v,e)->{
   if(e.getActionMasked()==MotionEvent.ACTION_DOWN){touchStart[0]=e.getX();touchStart[1]=e.getY();return true;}
   if(e.getActionMasked()==MotionEvent.ACTION_UP){
    float dx=e.getX()-touchStart[0],dy=e.getY()-touchStart[1];
    if(Math.abs(dx)<30 && Math.abs(dy)<30)v.performClick();
    else if(Math.abs(dx)>60 && Math.abs(dx)>Math.abs(dy))command(dx>0?"forward":"back");
    return true;
   }
   return true;
  });
  // WebView keeps native YouTube controls interactive. Use keys/gamepad/remote for YouTube seek.
  try {
   String token=UUID.randomUUID().toString().replace("-", "");
   server=new RemoteServer(token,asset("remote.html"),this); server.start(5000,false);
   ArrayList<String> addresses=new ArrayList<>();
   for(NetworkInterface ni:Collections.list(NetworkInterface.getNetworkInterfaces())) {
    if(!ni.isUp()||ni.isLoopback()) continue;
    for(InetAddress a:Collections.list(ni.getInetAddresses())) if(a instanceof Inet4Address && a.isSiteLocalAddress()) addresses.add("http://"+a.getHostAddress()+":8080/#"+token);
   }
   pairing=addresses.isEmpty()?"No local Wi-Fi address. Connect glasses to Wi-Fi and reopen GlassPlayer.":"On iPhone, connect to the same Wi-Fi and open in Safari:\n\n"+String.join("\n\n",addresses)+"\n\nKeep GlassPlayer open. Pairing changes whenever the app restarts.";
  } catch(Exception e) { pairing="Receiver could not start: "+e.getMessage(); }
  handler.post(poll); showPairing();
 }
 private String asset(String name) throws IOException { try(InputStream in=getAssets().open(name); ByteArrayOutputStream out=new ByteArrayOutputStream()) {byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);return out.toString("UTF-8");} }
 private static String format(long ms) { if(ms<0)return "--:--";return String.format(Locale.US,"%d:%02d",ms/60000,(ms/1000)%60); }
 private void showPairing() { new AlertDialog.Builder(this).setTitle("Pair your iPhone").setMessage(pairing).setPositiveButton("Done",null).show(); }
 private void input() { EditText input=new EditText(this); input.setSingleLine();input.setHint("YouTube link or media URL");new AlertDialog.Builder(this).setTitle("Open video").setView(input).setPositiveButton("Play",(d,w)->{try{play(VideoSource.parse(input.getText().toString()));}catch(Exception e){status.setText(e.getMessage());}}).setNegativeButton("Cancel",null).show(); }
 @Override public void play(VideoSource source) { runOnUiThread(()->{
  if(closed) return;
  handler.removeCallbacks(poll); handler.post(poll);
  player.stop(); web.loadUrl("about:blank"); youtube=source.youtubeId!=null;
  web.setVisibility(youtube?View.VISIBLE:View.GONE); playerView.setVisibility(youtube?View.GONE:View.VISIBLE);
  status.setText("Loading…");
  if(youtube) try { String html=asset("youtube.html").replace("__VIDEO_ID__",source.youtubeId); web.loadDataWithBaseURL("https://ph.marker.glassplayer/",html,"text/html","UTF-8",null); }catch(IOException e){status.setText("YouTube player asset missing");}
  else { player.setMediaItem(MediaItem.fromUri(source.url)); player.prepare(); player.play(); }
 }); }
 @Override public void command(String action) { runOnUiThread(()->{
  if(closed) return;
  if(action.equals("volumeUp")||action.equals("volumeDown")){ ((AudioManager)getSystemService(AUDIO_SERVICE)).adjustStreamVolume(AudioManager.STREAM_MUSIC,action.equals("volumeUp")?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,0);return; }
  if(action.equals("stop")){player.stop();web.loadUrl("about:blank");youtube=false;web.setVisibility(View.GONE);playerView.setVisibility(View.VISIBLE);player.clearMediaItems();status.setText("GLASSPLAYER • Ready");return;}
  if(youtube) { web.evaluateJavascript("window.glassCommand && window.glassCommand("+JSONObject.quote(action)+")",null);return; }
  switch(action){case "toggle":if(player.isPlaying())player.pause();else player.play();break;case "back":player.seekTo(Math.max(0,player.getCurrentPosition()-10000));break;case "forward":long next=player.getCurrentPosition()+10000;player.seekTo(player.getDuration()>0?Math.min(next,player.getDuration()):next);break;}
 }); }
 @Override public boolean dispatchKeyEvent(KeyEvent event) {
  int key=event.getKeyCode();
  String a=null;
  switch(key){case KeyEvent.KEYCODE_DPAD_CENTER:case KeyEvent.KEYCODE_BUTTON_A:case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:case KeyEvent.KEYCODE_SPACE:a="toggle";break;case KeyEvent.KEYCODE_DPAD_LEFT:case KeyEvent.KEYCODE_MEDIA_REWIND:a="back";break;case KeyEvent.KEYCODE_DPAD_RIGHT:case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:a="forward";break;case KeyEvent.KEYCODE_DPAD_UP:a="volumeUp";break;case KeyEvent.KEYCODE_DPAD_DOWN:a="volumeDown";break;}
  if(a!=null){if(event.getAction()==KeyEvent.ACTION_DOWN && event.getRepeatCount()==0)command(a);return true;}return super.dispatchKeyEvent(event);
 }
 @Override protected void onDestroy(){closed=true;handler.removeCallbacksAndMessages(null);if(server!=null)server.stop();if(player!=null)player.release();if(web!=null){web.stopLoading();web.destroy();}super.onDestroy();}
}
