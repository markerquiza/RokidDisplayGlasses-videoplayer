package ph.marker.glassplayer;

import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

final class RemoteServer extends NanoHTTPD {
 interface Receiver { void play(VideoSource source); void command(String action); }
 private final String token, html;
 private final Receiver receiver;
 RemoteServer(String token, String html, Receiver receiver) { super(8080); this.token=token; this.html=html; this.receiver=receiver; }
 private Response reply(Response.Status status,String type,String body) {
  Response r=newFixedLengthResponse(status,type,body);
  r.addHeader("Cache-Control","no-store"); r.addHeader("X-Content-Type-Options","nosniff");
  r.addHeader("Referrer-Policy","no-referrer");
  r.addHeader("Content-Security-Policy","default-src 'self'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; connect-src 'self'; frame-ancestors 'none'; form-action 'self'");
  return r;
 }
 @Override public Response serve(IHTTPSession session) {
  if (session.getMethod()==Method.GET && session.getUri().equals("/")) return reply(Response.Status.OK,"text/html; charset=utf-8",html);
  if (session.getMethod()!=Method.POST || !session.getUri().equals("/api")) return reply(Response.Status.NOT_FOUND,"text/plain","Not found");
  String supplied=session.getHeaders().getOrDefault("x-pair-token", "");
  if (!MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8))) return reply(Response.Status.UNAUTHORIZED,"text/plain","Pair again using the address shown on the glasses.");
  try {
   int length=Integer.parseInt(session.getHeaders().getOrDefault("content-length","0"));
   if(length<1||length>8192 || session.getHeaders().containsKey("transfer-encoding")) return reply(Response.Status.BAD_REQUEST,"text/plain","Invalid request size");
   if(!session.getHeaders().getOrDefault("content-type","").startsWith("application/x-www-form-urlencoded")) return reply(Response.Status.BAD_REQUEST,"text/plain","Expected form data");
   session.parseBody(new HashMap<>());
   Map<String,String> p=session.getParms();
   String action=p.getOrDefault("action", "");
   if(action.equals("load")) receiver.play(VideoSource.parse(p.getOrDefault("url","")));
   else if(action.matches("toggle|back|forward|volumeUp|volumeDown|stop")) receiver.command(action);
   else throw new IllegalArgumentException("Unknown command");
   return reply(Response.Status.OK,"text/plain","Command sent to glasses");
  } catch(IllegalArgumentException e) { return reply(Response.Status.BAD_REQUEST,"text/plain",e.getMessage()); }
  catch(Exception e) { return reply(Response.Status.INTERNAL_ERROR,"text/plain","Unable to process request"); }
 }
}
