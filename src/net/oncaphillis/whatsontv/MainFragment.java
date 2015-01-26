package net.oncaphillis.whatsontv;

import info.movito.themoviedbapi.model.tv.TvSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.oncaphillis.whatsontv.SearchThread.Current;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.Fragment;

public class MainFragment extends Fragment {
	private Activity _activity = null;
	private View _rootView = null;
	private ProgressBar  _progressBar = null;

	private GridView _mainGridView = null;
	
	private int _idx;
	
	@Override
    public View onCreateView(LayoutInflater inflater,
    		ViewGroup container, Bundle savedInstanceState) {
		Bundle b = this.getArguments();
		
		_idx  = b.getInt("idx");
		
		int cols = 1;

		if(_activity!=null){
			DisplayMetrics displaymetrics = new DisplayMetrics();
			_activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			
			float width  = displaymetrics.widthPixels * 160.0f / displaymetrics.xdpi;
			
			if(width > 400.0f)
				cols=2;
			if(width > 800.0f)
				cols=3;
			if(width > 1000.0f)
				cols=4;
		}
		
		_rootView   = inflater.inflate(R.layout.main_fragment, container, false);
				
		_progressBar  = (ProgressBar)_rootView.findViewById(R.id.load_progress);
		
		setProgressBarVisibility(true);
		setProgressBarIndeterminate(true);
		
		_mainGridView    = (GridView)    _rootView.findViewById(R.id.main_list);
		
		_mainGridView.setNumColumns(cols);
		
			Object o = MainActivity.ListAdapters[_idx];
				
			_mainGridView.setAdapter(MainActivity.ListAdapters[_idx]);
			
			_mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					Intent myIntent = new Intent(_activity, SeriesPagerActivity.class);
					Bundle b        = new Bundle();
					synchronized(MainActivity.ListAdapters[_idx]) {
						int[]    ids   = new int[MainActivity.ListAdapters[_idx].getCount()];
						String[] names = new String[MainActivity.ListAdapters[_idx].getCount()];
						
						for(int ix=0;ix<MainActivity.ListAdapters[_idx].getCount();ix++) {
							ids[ix]   = MainActivity.ListAdapters[_idx].getItem(ix).getId();
							names[ix] = MainActivity.ListAdapters[_idx].getItem(ix).getName();
						}
						
						b.putString(SeriesObjectFragment.ARG_TITLE, MainActivity.Titles[_idx]);
						b.putIntArray("ids", ids);
						b.putStringArray("names", names);
						b.putInt("ix", position);
					
						myIntent.putExtras(b);
						startActivity(myIntent);
					}
				}
			});
        return _rootView;
    }
		
	@Override
	public void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onAttach(Activity act) {
        _activity = act;
        super.onAttach(act);
	}

	public int getIdx() {
		return _idx;
	}

	public void setProgressBarVisibility(boolean b) {
		if(_progressBar!=null) {
			_progressBar.setVisibility( b ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public void setProgressBarIndeterminate(boolean b) {
		if(_progressBar!=null) {
			_progressBar.setIndeterminate(b);
		}
	}

	public void setProgress(int i) {
		if(_progressBar!=null) {
			_progressBar.setProgress(i);
		}
	}
}
