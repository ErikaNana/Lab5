package edu.calpoly.android.lab5;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * This class binds the visual JokeViews and the data behind them (Jokes).
 */
public class JokeListAdapter extends BaseAdapter {

	/** The application Context in which this JokeListAdapter is being used. */
	private Context m_context;

	/** The data set to which this JokeListAdapter is bound. */
	private List<Joke> m_jokeList;

	/**
	 * Parameterized constructor that takes in the application Context in which
	 * it is being used and the Collection of Joke objects to which it is bound.
	 * m_nSelectedPosition will be initialized to Adapter.NO_SELECTION.
	 * 
	 * @param context
	 *            The application Context in which this JokeListAdapter is being
	 *            used.
	 * 
	 * @param jokeList
	 *            The Collection of Joke objects to which this JokeListAdapter
	 *            is bound.
	 */
	public JokeListAdapter(Context context, List<Joke> jokeList) {
		this.m_context = context;
		this.m_jokeList = jokeList;
	}
	
	/**
	 * returns the number of items in the dataset(m_jokeList)
	 */
	@Override
	public int getCount() {
		return this.m_jokeList.size();
	}

	/**
	 * returns the Joke object from the dataset at the specified position
	 */
	@Override
	public Object getItem(int position) {
		return this.m_jokeList.get(position);
	}
	/**
	 * Usually returns the id of the item at the position in the list.  However, use the 
	 * Joke's position as its unique Id for the lab
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	/**
	 * Returns a JokeView object for the Joke object at the position in the dataset specified
	 * by position.
	 * The convertView object allows you to re-use a previously constructed view for better
	 * performance.  Since convertView is a View object that was previously returned by
	 * JokeListAdapter.getView() then you can safely assume it is a JokeView
	 * Parent parameter represents the container the returned JokeView will get added to.  
	 * Don't need to use this, but in some cases it can provide useful information.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView != null) {
			((JokeView) convertView).setJoke(this.m_jokeList.get(position));
			return convertView;
		}
		else {
			JokeView jokeview = new JokeView(this.m_context, this.m_jokeList.get(position));
			return jokeview;
		}
	}
}
