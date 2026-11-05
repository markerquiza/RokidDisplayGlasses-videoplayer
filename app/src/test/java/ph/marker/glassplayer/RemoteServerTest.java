package ph.marker.glassplayer;
import org.junit.*;
import static org.junit.Assert.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
public class RemoteServerTest {
 RemoteServer server;
 volatile String received;
 @Before public void start() throws Exception {
  server=new RemoteServer("test-token","<html>Remote</html>",new RemoteServer.Receiver(){public void play(VideoSource s){received=s.url;}public void command(String a){received=a;}});
  server.start(1000,false);
 }
 @After public void stop(){server.stop();}
 private int post(String token,String body) throws Exception {
  HttpURLConnection c=(HttpURLConnection)new URL("http://127.0.0.1:8080/api").openConnection();
  c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(2000);c.setReadTimeout(2000);
  c.setRequestProperty("X-Pair-Token",token);c.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
  c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));int code=c.getResponseCode();c.disconnect();return code;
 }
 @Test public void unauthorizedCannotControlPlayer() throws Exception {assertEquals(401,post("wrong","action=toggle"));assertNull(received);}
 @Test public void validCommandAndMediaReachReceiver() throws Exception {
  assertEquals(200,post("test-token","action=toggle"));assertEquals("toggle",received);
  assertEquals(200,post("test-token","action=load&url=https%3A%2F%2Fexample.com%2Fmovie.mp4"));assertEquals("https://example.com/movie.mp4",received);
 }
 @Test public void rejectsInvalidInput() throws Exception {
  assertEquals(400,post("test-token","action=load&url=javascript%3Aalert(1)"));assertNull(received);
  assertEquals(400,post("test-token","action=destroy"));assertNull(received);
 }
 @Test public void getCannotIssueCommands() throws Exception {
  HttpURLConnection c=(HttpURLConnection)new URL("http://127.0.0.1:8080/api?action=toggle").openConnection();
  assertEquals(404,c.getResponseCode());assertNull(received);c.disconnect();
 }
}
