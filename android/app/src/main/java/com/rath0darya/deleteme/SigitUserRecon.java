package com.rath0darya.deleteme;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/** Android port of SIGIT userrecon.
 * Checks public profile URLs only. No credentials, cookies, private data, or login bypass.
 */
public final class SigitUserRecon {
 public static final class Match { public final String source,url,status; Match(String s,String u,String st){source=s;url=u;status=st;} }
 private static final String[][] SITES={
  {"Facebook","https://facebook.com/%s"},
  {"Instagram","https://instagram.com/%s"},
  {"X","https://twitter.com/%s"},
  {"YouTube","https://youtube.com/%s"},
  {"Vimeo","https://vimeo.com/%s"},
  {"GitHub","https://github.com/%s"},
  {"Google+","https://plus.google.com/%s"},
  {"Pinterest","https://pinterest.com/%s"},
  {"Flickr","https://flickr.com/people/%s"},
  {"VK","https://vk.com/%s"},
  {"About.me","https://about.me/%s"},
  {"Disqus","https://disqus.com/%s"},
  {"Bitbucket","https://bitbucket.org/%s"},
  {"Flipboard","https://flipboard.com/@%s"},
  {"Medium","https://medium.com/@%s"},
  {"HackerOne","https://hackerone.com/%s"},
  {"Keybase","https://keybase.io/%s"},
  {"BuzzFeed","https://buzzfeed.com/%s"},
  {"SlideShare","https://slideshare.net/%s"},
  {"Mixcloud","https://mixcloud.com/%s"},
  {"SoundCloud","https://soundcloud.com/%s"},
  {"Badoo","https://badoo.com/en/%s"},
  {"Imgur","https://imgur.com/user/%s"},
  {"Spotify","https://open.spotify.com/user/%s"},
  {"Pastebin","https://pastebin.com/u/%s"},
  {"Wattpad","https://wattpad.com/user/%s"},
  {"Canva","https://canva.com/%s"},
  {"Codecademy","https://codecademy.com/%s"},
  {"Last.fm","https://last.fm/user/%s"},
  {"Blip.fm","https://blip.fm/%s"},
  {"Dribbble","https://dribbble.com/%s"},
  {"Gravatar","https://en.gravatar.com/%s"},
  {"Foursquare","https://foursquare.com/%s"},
  {"Creative Market","https://creativemarket.com/%s"},
  {"Ello","https://ello.co/%s"},
  {"Cash.me","https://cash.me/%s"},
  {"AngelList","https://angel.co/%s"},
  {"500px","https://500px.com/%s"},
  {"Houzz","https://houzz.com/user/%s"},
  {"Tripadvisor","https://tripadvisor.com/members/%s"},
  {"Kongregate","https://kongregate.com/accounts/%s"},
  {"Blogspot","https://%s.blogspot.com/"},
  {"Tumblr","https://%s.tumblr.com/"},
  {"WordPress","https://%s.wordpress.com/"},
  {"DeviantArt","https://%s.devianart.com/"},
  {"Slack","https://%s.slack.com/"},
  {"LiveJournal","https://%s.livejournal.com/"},
  {"Newgrounds","https://%s.newgrounds.com/"},
  {"HubPages","https://%s.hubpages.com"},
  {"Contently","https://%s.contently.com"},
  {"Steam","https://steamcommunity.com/id/%s"},
  {"Wikipedia","https://www.wikipedia.org/wiki/User:%s"},
  {"Freelancer","https://www.freelancer.com/u/%s"},
  {"Dailymotion","https://www.dailymotion.com/%s"},
  {"Etsy","https://www.etsy.com/shop/%s"},
  {"Scribd","https://www.scribd.com/%s"},
  {"Patreon","https://www.patreon.com/%s"},
  {"Behance","https://www.behance.net/%s"},
  {"Goodreads","https://www.goodreads.com/%s"},
  {"Gumroad","https://www.gumroad.com/%s"},
  {"Instructables","https://www.instructables.com/member/%s"},
  {"CodeMentor","https://www.codementor.io/%s"},
  {"ReverbNation","https://www.reverbnation.com/%s"},
  {"Designspiration","https://www.designspiration.net/%s"},
  {"Bandcamp","https://www.bandcamp.com/%s"},
  {"ColourLovers","https://www.colourlovers.com/love/%s"},
  {"IFTTT","https://www.ifttt.com/p/%s"},
  {"Trakt","https://www.trakt.tv/users/%s"},
  {"OKCupid","https://www.okcupid.com/profile/%s"},
  {"SkyScanner","https://www.trip.skyscanner.com/user/%s"},
  {"Zone-H","http://www.zone-h.org/archive/notifier=%s"}
 };
 private SigitUserRecon(){}
 public static List<Match> scan(String username){
  String u=username==null?"":username.trim(); List<Match> out=new ArrayList<>(); if(u.isEmpty())return out;
  for(String[] site:SITES){String url=String.format(site[1],u);try{int code=head(url);if(code>=200&&code<400)out.add(new Match(site[0],url,"Public profile response (HTTP "+code+")"));}catch(Exception ignored){}}
  return out;
 }
 private static int head(String url)throws IOException{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod("HEAD");c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(true);c.setRequestProperty("User-Agent","DeleteMe-Username-Recon/1.0");try{return c.getResponseCode();}finally{c.disconnect();}}
}
