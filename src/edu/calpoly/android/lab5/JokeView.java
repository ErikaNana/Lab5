package edu.calpoly.android.lab5;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

//RadioGroup.OnCheckedChangeListener
//interface definition for a callback to be invoked when the checked radio button changed
//in the group
public class JokeView extends LinearLayout implements OnCheckedChangeListener{

	/** Radio buttons for liking or disliking a joke. */
	private RadioButton m_vwLikeButton;
	private RadioButton m_vwDislikeButton;
	
	/** The container for the radio buttons. */
	private RadioGroup m_vwLikeGroup;

	/** Displays the joke text. */
	private TextView m_vwJokeText;
	
	/** The data version of this View, containing the joke's information. */
	private Joke m_joke;
	
	/**
	 * Basic Constructor that takes only an application Context.
	 * 
	 * @param context
	 *            The application Context in which this view is being added. 
	 *            
	 * @param joke
	 * 			  The Joke this view is responsible for displaying.
	 */
	public JokeView(Context context, Joke joke) {
		super(context);

		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//inflating joke_view.xml
		//after layout is inflated, want this JokeView object to be the root ViewGroup of the
		//inflated layout.  Instead of returning an inflated hierarchy of View, this JokeView
		//object will become the root of that hierarchy
		inflater.inflate(R.layout.joke_view, this, true);
		
		//initialize all the View component member variables
		this.m_vwLikeButton = (RadioButton) findViewById(R.id.likeButton);
		this.m_vwDislikeButton = (RadioButton) findViewById(R.id.dislikeButton);
		this.m_vwLikeGroup = (RadioGroup) findViewById(R.id.ratingRadioGroup);
		this.m_vwJokeText = (TextView) findViewById(R.id.jokeTextView);
		
		setJoke(joke);
		//setting the OnCheckedLisneter for m_vwLikeGroup to this JokeView object
		m_vwLikeGroup.setOnCheckedChangeListener(this);
	}

	/**
	 * Mutator method for changing the Joke object this View displays. This View
	 * will be updated to display the correct contents of the new Joke.
	 * 
	 * @param joke
	 *            The Joke object which this View will display.
	 */
	public void setJoke(Joke joke) {
		//update joke reference with the joke passed in
		this.m_joke = joke;
		//update m_vwJokeText with the text for the new joke
		this.m_vwJokeText.setText(joke.getJoke());
		
		//by ellipsing the joke TextView and making the rating RadioGroup disappear we have
		//changed the size of the JokeView.  This has caused the JokeView to become
		//invalidated.  Whenever a view becomes invalidated it should request to be laid out
		//again.  Failing to make this call will result in the view not being updated
		//properly.
		this.requestLayout();

		//setting the checked state to true on the appropriate RadioButton to reflect the
		//rating for the new joke
		int rating = joke.getRating();
		
		switch (rating){
			case Joke.LIKE:{
				this.m_vwLikeButton.setChecked(true);
				this.m_vwDislikeButton.setChecked(false);
				break;
			}
			case Joke.DISLIKE:{
				if (joke.getRating() == Joke.DISLIKE) {
					this.m_vwDislikeButton.setChecked(true);
					this.m_vwLikeButton.setChecked(false);
				}
				break;
			}
			case Joke.UNRATED:{
				this.m_vwLikeGroup.clearCheck();
				break;
			}
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (group.equals(m_vwLikeGroup)) {
			//if like button is checked, change the rating of the joke in JokeView
			if(checkedId == R.id.likeButton) {
				this.m_joke.setRating(Joke.LIKE);
			}
			//if dislike button is checked, change the rating of the joke in JokeView
			if(checkedId == R.id.dislikeButton) {
				this.m_joke.setRating(Joke.DISLIKE);
			}
		}
		
	}
	
	/**
	 * Helper method
	 * @return text of the joke
	 */
	public String getText() {
		return m_joke.getJoke();
	}
	
	/**
	 * Helper method
	 * @return actual joke
	 */
	public Joke getJoke() {
		return m_joke;
	}

}