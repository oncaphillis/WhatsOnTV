package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.Network;
import info.movito.themoviedbapi.model.tv.TvEpisode;
import info.movito.themoviedbapi.model.tv.TvSeason;
import info.movito.themoviedbapi.model.tv.TvSeries;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import net.oncaphillis.whatsontv.Tmdb.EpisodeInfo;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

class SeriesInfo {
	private TvSeries _tvs;
	private String _nws = new String("");
	private Calendar _lastAired  = null;
	private String _lastAiredStr = null;
	private String _nextAiring   = null;
	private String _title = null; 
	public SeriesInfo(TvSeries s) {
		_tvs = s;
		if(_tvs.getNetworks()!=null) {
			for(Network nw : _tvs.getNetworks() )  {
				_nws += (_nws.isEmpty() ? "" : " ") + nw.getName();
			}
		}
		
		if(s.getLastAirDate()!=null) {
			
			Calendar c    = TimeTool.fromString(s.getLastAirDate());
	        _lastAired    = c;
	        _lastAiredStr = TimeTool.toString(_lastAired);

			// All TMDB dates are EST.
	        // We check if the Last Aired Field is >= Today
	        // I this case we search for the smallest airing date >= today
	        
	        Calendar td = TimeTool.toTimeZone(TimeTool.getToday(),"EST");

	        if( ! td.after( _lastAired)  ) {
	        	if(s.getSeasons()!=null) {
	        		ListIterator<TvSeason> season_iterator = s.getSeasons().listIterator(s.getSeasons().size());
	        		while(_nextAiring == null && season_iterator.hasPrevious()) {
		        		TvSeason  season = Tmdb.get().loadSeason(s.getId(), season_iterator.previous().getSeasonNumber());
		        		if(season.getEpisodes()!=null) {

		        			ListIterator<TvEpisode> episode_iterator = season.getEpisodes().listIterator(season.getEpisodes().size());

		        			EpisodeInfo le = null;

		        			while(_nextAiring == null && episode_iterator.hasPrevious()) {
			        			
			        			EpisodeInfo episode = Tmdb.get().loadEpisode(s.getId(), season.getSeasonNumber(), episode_iterator.previous().getEpisodeNumber());
			        			if(episode.getTmdb().getAirDate()!=null && le!=null && TimeTool.fromString(episode.getTmdb().getAirDate()).before(td) ) {
			        				if(le.getAirTime()!=null)
			        					_nextAiring = TimeTool.toString(le.getAirTime());
			        				_title = le.getTmdb().getName();
			        				break;
			        			}
			        			le = episode;
			        		}
		        		}
		        	}
	        	}
	        } else {
	        	s = Tmdb.get().loadSeries(s.getId());
	        	if(s.getSeasons()!=null && s.getSeasons().size()>0) {
	        		TvSeason ts = Tmdb.get().loadSeason(s.getId(),s.getSeasons().get(s.getSeasons().size()-1).getSeasonNumber());
	        		if(ts.getEpisodes()!=null && ts.getEpisodes().size()>0) {
	        			EpisodeInfo eps = Tmdb.get().loadEpisode(s.getId(),ts.getSeasonNumber(),ts.getEpisodes().get(ts.getEpisodes().size()-1).getEpisodeNumber());
	        			_title = eps.getTmdb().getName();
	        		}
	        	}
	        }
		}
	}

	Calendar getLastAired() {
		return _lastAired;
	}

	String getLastAiredStr() {
		return _lastAiredStr;
	}
	
	String getNextAiring() {
		return _nextAiring; 
	}
	
	String getNetworks() {
		return _nws;
	}

	public String getTitle() {
		return _title;
	}
};

public class SeriesInfoDownLoaderTask extends AsyncTask<String, Void, SeriesInfo> {
	private WeakReference<TextView> _networkText;
	private WeakReference<TextView> _timeText;
	private Activity _activity;
	
	public SeriesInfoDownLoaderTask(TextView networkText, TextView timeText,Activity activity) {
		_networkText = new WeakReference(networkText);
		_timeText    = new WeakReference(timeText);
		
		_activity = activity;
	}

	@Override
	protected SeriesInfo doInBackground(String... params) {
		
		SeriesInfo si = null;
		if(_networkText != null && _networkText.get() != null && _networkText.get().getTag()!=null && _networkText.get().getTag() instanceof Integer) {
			TvSeries s = Tmdb.get().loadSeries((Integer)_networkText.get().getTag());
			si = new SeriesInfo(s);			
		}
		return si;
	}	

	@Override
	protected void onPostExecute(SeriesInfo si) {
		// dates reported by Tmdb are in EST. We need this to compare
		// the last aired
		Calendar today1 = TimeTool.toTimeZone(TimeTool.getToday(),"EST");

		if(si!=null) {
			if(_timeText != null && _timeText.get() != null && _timeText.get().getTag()!=null && _timeText.get().getTag() instanceof Integer) {
				if(si.getNextAiring()!=null) {
					_timeText.get().setText(si.getNextAiring()+" '"+(si.getTitle()== null ? "" : si.getTitle())+"'");					
					_timeText.get().setTextColor(_activity.getResources().getColor(R.color.oncaphillis_orange));
				} else {
					_timeText.get().setText(si.getLastAiredStr()+" '"+(si.getTitle()== null ? "" : si.getTitle())+"'");
					_timeText.get().setTextColor(_activity.getResources().getColor(R.color.actionbar_text_color));
				}
			}

			if(_networkText != null && _networkText.get() != null && _networkText.get().getTag()!=null && _networkText.get().getTag() instanceof Integer) {
				_networkText.get().setText(si.getNetworks());
			}
		}
	}
}
