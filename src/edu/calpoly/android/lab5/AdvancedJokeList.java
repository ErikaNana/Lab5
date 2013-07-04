package edu.calpoly.android.lab5;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

public class AdvancedJokeList extends SherlockActivity {
	
	/** Contains the name of the Author for the jokes. */
	protected String m_strAuthorName;

	/** Contains the list of Jokes the Activity will present to the user. */
	protected ArrayList<Joke> m_arrJokeList;
	
	/** Contains the list of filtered Jokes the Activity will present to the user. */
	protected ArrayList<Joke> m_arrFilteredJokeList;

	/** Adapter used to bind an AdapterView to List of Jokes. */
	protected JokeListAdapter m_jokeAdapter;

	/** ViewGroup used for maintaining a list of Views that each display Jokes. */
	protected ListView m_vwJokeLayout;

	/** EditText used for entering text for a new Joke to be added to m_arrJokeList. */
	protected EditText m_vwJokeEditText;

	/** Button used for creating and adding a new Joke to m_arrJokeList using the
	 *  text entered in m_vwJokeEditText. */
	protected Button m_vwJokeButton;
	
	/** Menu used for filtering Jokes. */
	protected Menu m_vwMenu;

	/** Background Color values used for alternating between light and dark rows
	 *  of Jokes. Add a third for text color if necessary. */
	protected int m_nDarkColor;
	protected int m_nLightColor;
	
	protected int m_nTextColor;
	
	/** Used to get specific JokeView when list of jokes is long-clicked */
	protected JokeView current_JokeView;
	
	/** Saved Filter Value*/
	protected static String SAVED_FILTER_VALUE;
	/**
	 * Context-Menu MenuItem IDs.
	 * IMPORTANT: You must use these when creating your MenuItems or the tests
	 * used to grade your submission will fail. These are commented out for now.
	 * These help identify which type of filter has been chosen when a user selects a SubMenu item
	 */
	protected static final int FILTER = Menu.FIRST;
	protected static final int FILTER_LIKE = SubMenu.FIRST;
	protected static final int FILTER_DISLIKE = SubMenu.FIRST + 1;
	protected static final int FILTER_UNRATED = SubMenu.FIRST + 2;
	protected static final int FILTER_SHOW_ALL = SubMenu.FIRST + 3;
	
	//filter value
	protected int filter = FILTER_SHOW_ALL;
	
	/** Key to store text m_vwJokeEditText in SharedPreferences */
	protected static final String SAVED_EDIT_TEXT = "saved_edit_text";
	
	//implement the ActionMode.Callback
	protected com.actionbarsherlock.view.ActionMode actionMode;
	protected com.actionbarsherlock.view.ActionMode.Callback callback = new com.actionbarsherlock.view.ActionMode.Callback() {
		
		//inflate Action Menu
		//Set Action Mode to terminate after the Remove item is selected
		
		/**
		 *Called when the action mode is created; startActionMode() was called
		 */
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//inflate action menu, for the context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.actionmenu, menu);
			return true;
		}
		
		
		
		/**
		 * Called each time the action mode is shown.  Always called after on CreateAction
		 */
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; //Return false if nothing is done
		}
		
		/**
		 * Called when the user selects a contextual menu item
		 */
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			//set ListView to have an OnItemLongClickListener to trigger the firing of
			//the Action Mode Callback
			Toast.makeText(getBaseContext(), "in callback", Toast.LENGTH_SHORT).show();
			switch(item.getItemId()) {
				case R.id.menu_remove:
					//find position of joke in the filtered array
					Joke actual_joke = current_JokeView.getJoke();
					if (m_arrFilteredJokeList.contains(actual_joke)) {
						int position = m_arrFilteredJokeList.indexOf(actual_joke);
						//delete the joke
						m_arrFilteredJokeList.remove(position);
						//notify the adapter
						m_jokeAdapter.notifyDataSetChanged();
						
						//also delete it from master list
						int positon_master = m_arrJokeList.indexOf(actual_joke);
						m_arrJokeList.remove(positon_master);
					}
					mode.finish(); //Action done, so close the CAB
					return true;
				case R.id.upload_menu:
					Joke joke = current_JokeView.getJoke();
					uploadJokeToServer(joke);
				default:
					return false;
			}
		}
		
		/**
		 * Called when the user exits the action mode
		 */
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			
		}
	};
	
	/**
	 * Set ListView to have an OnItemLongClickListener
	 */
	protected void initLongClickListener() {
		Toast.makeText(getBaseContext(), "in long click method", Toast.LENGTH_SHORT).show();
		m_vwJokeLayout.requestFocus();
		m_vwJokeLayout.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> av, View view,
					int pos, long id) {
				if (actionMode != null) {
					return false;
				}
				
				//for future use
				current_JokeView = (JokeView) view;
				//Start the CAB using the ActionMode.Callback defined above
				actionMode = startActionMode(callback);
				return true;
			}
		});
	}
	
	/**
	 * Initializes everything when app is created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.initLayout();
		
		//getting the colors from the XML file, which automatically generates code in
		//R.java.  Then use R.java to get the colors
		Resources resources = this.getResources();
		this.m_nDarkColor = resources.getInteger(R.color.dark);
		this.m_nLightColor = resources.getInteger(R.color.light);
		this.m_nTextColor = resources.getInteger(R.color.text);
		
		this.m_arrJokeList = new ArrayList<Joke>(); //initialize to new instance
		this.m_arrFilteredJokeList = new ArrayList<Joke>();
		
		//get array of joke strings
		String[] joke_strings = resources.getStringArray(R.array.jokeList); 
		
		//set the author name
		this.m_strAuthorName = resources.getString(R.string.author_name);
		
		//for each of the strings in joke_strings, make call to addJoke
		for (String joke_string : joke_strings) {
			Joke joke = new Joke(joke_string, this.m_strAuthorName);
			this.addJoke(joke);
		}
	
		//initialize m_jokeAdapter member variable with ArrayList of jokes
		//need to bind to m_arrFilteredJokeList
		this.m_jokeAdapter = new JokeListAdapter(this, m_arrFilteredJokeList);
			
		//set m_vwJokeLayout's adapter to be m_jokeAdapter
		this.m_vwJokeLayout.setAdapter(m_jokeAdapter);
		
		//restoring SharedPreference Data
		//retrieve the private SharedPreferences belong to this activity
		SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
		//retrieve the text that was saved
		String retrieved_text = preferences.getString(SAVED_EDIT_TEXT, "");
		Toast.makeText(getBaseContext(), "current text:  " + retrieved_text, Toast.LENGTH_SHORT).show();
		//set text in m_vwJokeEditText to the text retrieved
		this.m_vwJokeEditText.setText(retrieved_text);
		this.m_jokeAdapter.notifyDataSetChanged();	
	}
	
	/**
	 * Filters the jokes based on the filter selection in the Action Bar and sets the filter
	 * variable
	 */
	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch(item.getItemId()) {
			//set the filter for sorting and display the appropriate jokes
			case R.id.submenu_like:
				filter = AdvancedJokeList.FILTER_LIKE;
				//Toast.makeText(this,"like jokes",Toast.LENGTH_SHORT).show();
				updateFilteredJokes(filter);
				//change menu's text
				onPrepareOptionsMenu(m_vwMenu);
				this.m_jokeAdapter.notifyDataSetChanged();				
				return true;

			case R.id.submenu_dislike:
				filter = AdvancedJokeList.FILTER_DISLIKE;
				updateFilteredJokes(filter);
				onPrepareOptionsMenu(m_vwMenu);
				this.m_jokeAdapter.notifyDataSetChanged();	
				return true;
				
			case R.id.submenu_unrated:
				filter = AdvancedJokeList.FILTER_UNRATED;
				updateFilteredJokes(filter);	
				onPrepareOptionsMenu(m_vwMenu);
				this.m_jokeAdapter.notifyDataSetChanged();	
				return true;
				
			case R.id.submenu_show_all:
				filter = AdvancedJokeList.FILTER_SHOW_ALL;
				updateFilteredJokes(filter);
				onPrepareOptionsMenu(m_vwMenu);
				this.m_jokeAdapter.notifyDataSetChanged();
				return true;
				
			case R.id.download_menu:
				getJokesFromServer();
				return true;
		}
		return false;
	};
	
	/**
	 *Restore instance data and override the default implementation 
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		//call super version to ensure that other UI state is preserved as well
		super.onRestoreInstanceState(savedInstanceState);
		//check to make sure savedInstanceState isn't null
		assert savedInstanceState != null;
		//check to make sure saved filter state is in the Bundle
		assert savedInstanceState.containsKey(SAVED_FILTER_VALUE);
		//retrieve the value of the filter from savedInstanceState
		int saved_filter = savedInstanceState.getInt(SAVED_FILTER_VALUE);
		//re-filter the joke list and notify adapter
		updateFilteredJokes(saved_filter);			
		this.m_jokeAdapter.notifyDataSetChanged();	
	}
	/**
	 * Create the filter menu
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		//use ABS's compatibility package
        MenuInflater inflater = getSupportMenuInflater();
        
        //menu = menu to inflate to
        inflater.inflate(R.menu.mainmenu, menu);
        //initialize m_vwMenu
        this.m_vwMenu = menu;
        return true;
    }
	/**
	 * Save the instance data and override default implementation
	 */
	@Override
	 protected void onSaveInstanceState(Bundle outState) {
		 //store the current value of filter in outState
		 outState.putInt(AdvancedJokeList.SAVED_FILTER_VALUE, filter);
		 //call super version of method to ensure that other UI state is preserved as well
		 super.onSaveInstanceState(outState);
	 }
	/**
	 * Method is used to encapsulate the code that initializes and sets the
	 * Layout for this Activity.
	 */
	protected void initLayout() {
		setContentView(R.layout.advanced);
		this.m_vwJokeLayout = (ListView) findViewById(R.id.jokeListViewGroup);
		this.m_vwJokeEditText = (EditText) findViewById(R.id.newJokeEditText);
		this.m_vwJokeButton = (Button) findViewById(R.id.addJokeButton);
		initAddJokeListeners();
		initLongClickListener();
	}

	/**
	 * Method is used to encapsulate the code that initializes and sets the
	 * Event Listeners which will respond to requests to "Add" a new Joke to the
	 * list.
	 */
	protected void initAddJokeListeners() {
		//setup the onClickListener for the "Add Joke" button.  
		//pass in reference to an Anonymous Inner Class that implements the OnClickListener interface
		//anonymous inner class = one-time use class that implements some interface
		//you declare the class and instantiate it in one motion
		
		m_vwJokeButton.setOnClickListener(new OnClickListener() {
			  public void onClick(View view) {
				  //retrieve text entered by user
				  String input_text = m_vwJokeEditText.getText().toString();
				  Joke joke = new Joke(input_text, "defaulto");
				  //clear the text in EditText
				  m_vwJokeEditText.setText("");
				  //add joke
				  addJoke(joke);
				  
				 //hide the Soft Keyboard that appears when the EditText has focus:
				 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromInputMethod(m_vwJokeEditText.getWindowToken(), 0);
			 } 
			});
		//implementation for the enter key
        m_vwJokeEditText.setOnKeyListener(new OnKeyListener(){

            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN){
                	if (arg1 == KeyEvent.KEYCODE_ENTER) {
                        String input_text = m_vwJokeEditText.getText().toString();
                        Joke joke = new Joke(input_text, m_strAuthorName);
                        addJoke(joke);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                        imm.hideSoftInputFromWindow(
                                m_vwJokeEditText.getWindowToken(), 0);
                	}
                }
                m_vwJokeEditText.setText("");
                return false;

            }

        });
	}
	/**
	 * Save data in a private SharedPreferences object
	 */
	@Override
	protected void onPause() {
		//always call the superclass method first
		super.onPause();
		//retrieve the private SharedPreferences belonging to this Activity
		//idk if this is right
		SharedPreferences preferences = getPreferences(Activity.MODE_PRIVATE);
	
		//create a new Editor for these preferences, through which you can make modifications
		//to the data in the preferences
		Editor editor = preferences.edit();
		
		//store the text in m_vwJokeEditText in the SharedPreferences
		String text = this.m_vwJokeEditText.getText().toString();
		Toast.makeText(getBaseContext(), "current text:  " + text, Toast.LENGTH_SHORT).show();
		editor.putString(AdvancedJokeList.SAVED_EDIT_TEXT, text);
		//need to call this to have any changes performed in the Editor show up in the 
		//Shared preferences
		editor.commit();
	}
	 /**
	  * Sets the filtered array of jokes based on the filter passed in.
	  * @param filter the filter for which the jokes should be filtered by
	  */
	protected void updateFilteredJokes (int filter) {
		//show only filtered jokes and clear the filtered list before starting
		if (!m_arrFilteredJokeList.isEmpty()) {
			m_arrFilteredJokeList.clear();
			this.m_jokeAdapter.notifyDataSetChanged();
		}
		
		switch (filter) {
			case AdvancedJokeList.FILTER_LIKE:{
				for(Joke joke: this.m_arrJokeList) {
					if(joke.getRating() == Joke.LIKE) {
						this.m_arrFilteredJokeList.add(joke);
					}
				}
				break;
			}
			case AdvancedJokeList.FILTER_DISLIKE:{
				for(Joke joke: this.m_arrJokeList) {
					if(joke.getRating() == Joke.DISLIKE) {
						this.m_arrFilteredJokeList.add(joke);
					}
				}
				break;
			}
			case AdvancedJokeList.FILTER_UNRATED:{
				for(Joke joke: this.m_arrJokeList) {
					if(joke.getRating() == Joke.UNRATED) {
						this.m_arrFilteredJokeList.add(joke);
					}
				}
				break;
			}
			case AdvancedJokeList.FILTER_SHOW_ALL:{
				for(Joke joke: this.m_arrJokeList) {
					this.m_arrFilteredJokeList.add(joke);
				}
				break;
			}
		}				
	}
	
	/**
	 * Returns the proper String of the passed in filter
	 */
	protected String getMenuTitleChange() {
		//get the titles from the resources file
		Resources resources = this.getResources();

		switch(filter) {
			case(FILTER_LIKE):{
				return resources.getString(R.string.like_menuitem);
			}
			case(FILTER_DISLIKE):{
				return resources.getString(R.string.dislike_menuitem);
			}
			case(FILTER_UNRATED):{
				return resources.getString(R.string.unrated_menuitem);
			}
			default:{
				return resources.getString(R.string.show_all_menuitem);
			}
		}
	}
	
	/**
	 * Changes the title text of the Filter menu item
	 * @param menu	options menu as last shown
	 */
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//get filter menu item
		MenuItem filter = menu.findItem(R.id.menu_filter);
	
		//get the name for the action bar based on the current filter
		String name = getMenuTitleChange();
		Toast.makeText(getBaseContext(), "current filter name:  " + name, Toast.LENGTH_SHORT).show();
		//set the title text of the filter
		filter.setTitle(name);
		
		//set the m_vwMenu variable
		m_vwMenu = menu;
		
		//ensure that other menu state is preserved as well
		super.onPrepareOptionsMenu(menu);
		
		//return true so menu is displayed
		return true;
	}
	/**
	 * Method used for encapsulating the logic necessary to properly add a new
	 * Joke to m_arrJokeList, and display it on screen.
	 * 
	 * @param joke
	 *            The Joke to add to list of Jokes.
	 */
	protected void addJoke(Joke joke) {
		this.m_arrJokeList.add(joke); 
		this.m_arrFilteredJokeList.add(joke);
	}
	
	/**
	 * Method used to retrieve Jokes from online server. For the duration of
	 * this method a ProgressDialog will be displayed showing the default
	 * spinner animation.
	 */
	protected void getJokesFromServer() {
		String toastText = "Starting Download";
		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
		toast.show();


		//TODO: create an AsyncTask inner class and call execute on it
		//TODO: use URL, Scanner, and InputStreamReader to download all jokes
		//TODO: use add jokes on the UI thread (in onPostExecute)
		//TODO: use Toast to notify the user that the download is complete
		try {
			URL url = new URL("http://simexusa.com/aac/getAllJokes.php?author=defaulto");

			new AsyncTask<URL, Void, Boolean>() {
				ArrayList<String> jokes = new ArrayList<String>();
				@Override
				protected Boolean doInBackground(URL... urls) {
					try {
						Scanner in;
						in = new Scanner(urls[0].openStream()); 
						in = in.useDelimiter("\n");
						
						while(in.hasNext()){
							jokes.add(in.next());

						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        return true;
			     }

			     protected void onPostExecute(Boolean success) {
			    	 String toastText = "Download Failed";
			    	 if (success) {
			    		 toastText = "Download Successful";
			    		 //clear lists
			    		 m_arrFilteredJokeList.clear();
			    		 m_arrJokeList.clear();
			    		 for (String joke: jokes) {
			    			 //don't inlcude the extra whitespace
			    			 if (joke.equals("")) {
			    				 break;
			    			 }
			    			 Joke new_joke = new Joke(joke,"defaulto");
			    			 //add the jokes back
			    			 m_arrFilteredJokeList.add(new_joke);
			    			 m_arrJokeList.add(new_joke);
			    		 }
			    	 }
	    			 //refresh the view
	    			 m_jokeAdapter.notifyDataSetChanged();
			    	 Toast toast = Toast.makeText(AdvancedJokeList.this, toastText, Toast.LENGTH_SHORT);
			    	 toast.show();
			     }

			}.execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method uploads a single Joke to the server. This method should test
	 * the response from the server and display success or failure to the user
	 * via a Toast Notification.
	 * 
	 * The addJoke script on the server requires two parameters, both of which
	 * should be encode in "UTF-8":
	 * 
	 * 1) "joke": The text of the joke.
	 * 
	 * 2) "author": The author of the joke.
	 * 
	 * @param joke
	 *            The Joke to be uploaded to the server.
	 * 
	 */
	protected void uploadJokeToServer(Joke joke) {
		String toastText = "Starting Upload";
		Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
		toast.show();
		
		// The following code accomplishes these things:
		// create an AsyncTask inner class and call execute on it
		// use URL to upload a joke to the server, and Scanner to check that it was successful
		// use Toast to notify if the upload was successful

		try {
			URL url = new URL("http://simexusa.com/aac/addOneJoke.php?" + "joke="
					+ URLEncoder.encode(joke.getJoke(), "UTF-8") + "&author="
					+ URLEncoder.encode(joke.getAuthor(), "UTF-8"));
			new AsyncTask<URL, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(URL... urls) {
					boolean success = false;
					try {
						Scanner in;
						in = new Scanner(urls[0].openStream()); 
						if (in.nextLine().equals("1 record added")) {
							success = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
			        return success;
			     }

			     protected void onPostExecute(Boolean success) {
			    	 String toastText = "Upload Failed";
			    	 if (success) {
			    		 toastText = "Upload Successful";
			    	 }
			    	 Toast toast = Toast.makeText(AdvancedJokeList.this, toastText, Toast.LENGTH_SHORT);
			    	 toast.show();
			     }

			}.execute(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}