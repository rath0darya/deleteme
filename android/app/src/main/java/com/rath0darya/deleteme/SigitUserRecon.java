package com.rath0darya.deleteme;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Safe Android port of SIGIT's public username-recon concept.
 * Checks only public profile URLs; no credentials, cookies, scraping of private data, or login bypass.
 */
public final class SigitUserRecon {
 public static final class Match { public final String source,url,status; Match(String s,String u,String st){source=s;url=u;status=st;} }
 private static final String[][] SITES={
  {"Facebook","https://facebook.com/%s"},{"Instagram","https://instagram.com/%s"},{"X","https://x.com/%s"},
  {"YouTube","https://youtube.com/@%s"},{"Vimeo","https://vimeo.com/%s"},{"GitHub","https://github.com/%s"},
  {"Pinterest","https://pinterest.com/%s"},{"Flickr","https://www.flickr.com/people/%s"},{"VK","https://vk.com/%s"},
  {"About.me","https://about.me/%s"},{"Disqus","https://disqus.com/%s"},{"Bitbucket","https://bitbucket.org/%s"},
  {"Medium","https://medium.com/@%s"},{"HackerOne","https://hackerone.com/%s"},{"Keybase","https://keybase.io/%s"},
  {"Slideshare","https://slideshare.net/%s"},{"Mixcloud","https://mixcloud.com/%s"},{"SoundCloud","https://soundcloud.com/%s"},
  {"Imgur","https://imgur.com/user/%s"},{"Spotify","https://open.spotify.com/user/%s"},{"Pastebin","https://pastebin.com/u/%s"},
  {"Wattpad","https://wattpad.com/user/%s"},{"Canva","https://canva.com/%s"},{"Codecademy","https://codecademy.com/%s"},
  {"Last.fm","https://last.fm/user/%s"},{"Dribbble","https://dribbble.com/%s"},{"Gravatar","https://en.gravatar.com/%s"},
  {"Foursquare","https://foursquare.com/user/%s"},{"Creative Market","https://creativemarket.com/%s"},{"500px","https://500px.com/%s"},
  {"Tripadvisor","https://tripadvisor.com/members/%s"},{"Steam","https://steamcommunity.com/id/%s"},{"Wikipedia","https://www.wikipedia.org/wiki/User:%s"},
  {"Freelancer","https://www.freelancer.com/u/%s"},{"Dailymotion","https://www.dailymotion.com/%s"},{"Etsy","https://www.etsy.com/shop/%s"},
  {"Scribd","https://www.scribd.com/%s"},{"Patreon","https://www.patreon.com/%s"},{"Behance","https://www.behance.net/%s"},
  {"Goodreads","https://www.goodreads.com/%s"},{"Gumroad","https://www.gumroad.com/%s"},{"Instructables","https://www.instructables.com/member/%s"},
  {"CodeMentor","https://www.codementor.io/%s"},{"Bandcamp","https://www.bandcamp.com/%s"},{"IFTTT","https://www.ifttt.com/p/%s"},
  {"Trakt","https://www.trakt.tv/users/%s"},{"OKCupid","https://www.okcupid.com/profile/%s"}
 };
 private SigitUserRecon(){}
 public static List<Match> scan(String username){
  String u=username==null?"":username.trim(); List<Match> out=new ArrayList<>(); if(u.isEmpty())return out;
  for(String[] site:SITES){String url=String.format(site[1],u);try{int code=head(url);if(code>=200&&code<400)out.add(new Match(site[0],url,"Public profile response (HTTP "+code+")"));}catch(Exception ignored){}}
  return out;
 }
 private static int head(String url)throws IOException{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod("HEAD");c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","DeleteMe-Username-Recon/1.0");try{return c.getResponseCode();}finally{c.disconnect();}}
}
