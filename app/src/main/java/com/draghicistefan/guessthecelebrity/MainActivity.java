package com.draghicistefan.guessthecelebrity;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
{
    //Declaring the views
    Button button0, button1, button2, button3;
    ImageView imageView;
    //Declaring the array lists that will contain the urls and names
    ArrayList<String> celebUrls = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    //The variable we will use to store the position of the chosen celebrity
    int chosenCeleb = 0;
    //The variable we will use to store the position of the correct celebrity
    int locationOfCorrectAnswer=0;
    //The array of strings containing the 4 options for the celebrity names
    String[] answers = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instantiating the views
        imageView = (ImageView) findViewById(R.id.imageView);

        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);

        //We instantiate a DownloadTask object to retrieve the url on another thread, based on the website's address
        DownloadTask task = new DownloadTask();
        String result = null;
        try
        {
            result = task.execute("http://www.posh24.com/celebrities").get();
            //We will only need a certain portion of the website, so we select only what is between the divs with a sidebarContainer class
            String[] splitResult = result.split("<div class=\"sidebarContainer\"");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find())
            {
                celebUrls.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find())
            {
                celebNames.add(m.group(1));
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        createNewQuestion();
    }

    //We retrieve the urls via a separate thread, using a class that extends the AsyncTask, which will take a string as a request and return another string
    public class DownloadTask extends AsyncTask<String, Void, String>
    {
        //We do all the retrieveing of the urls in the doInBackground method
        @Override
        protected String doInBackground(String... urls)
        {
            //we initialize a string to null. Later this will be the url
            String result = "";
            //We declare the url and the HttpURLConnection that we will use to connect to the data source
            URL url;
            HttpURLConnection connection = null;
            try
            {
                //We initialize the url and the connection
                url = new URL(urls[0]);
                connection = (HttpURLConnection) url.openConnection();
                //We will need a InputStream and InputStreamReader objects to store ncoming data from the connection
                InputStream in = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                //We store data red from the input stream reader into a variable
                int data = reader.read();
                //We pass thru the stored information and, as long it's not null, we convert the data into character,
                // which we then append to our original result variable tu build the url
                while (data != -1)
                {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                //After we got all the data we return the result
                return result;
            } catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    //We do the same procedure to retrieve the image
    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls)
        {
            try
            {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    //The method we use to return the celebrity that the user has chosen
    public void celebChosen(View view)
    {
        //If the user user guesses the correct answer we show a toast message to let him know that he guessed,
        // otherwise we let him know that he was wrong. Either way, after he has chosen,
        // we clear the screen and load another image with 4 options
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer)))
        {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        } else
        {
            Toast.makeText(getApplicationContext(), "Wrong! It was !" + celebNames.get(chosenCeleb), Toast.LENGTH_LONG).show();
        }
        createNewQuestion();
    }

    //The method we use to create a question
    public void createNewQuestion()
    {
        //We initialize a new Random object
        Random random = new Random();
        //We use the random instance to randomly select a position from our url array
        chosenCeleb = random.nextInt(celebUrls.size());

        //We then create a new instance of the image downloader task to download an image,
        // convert it into a bitmap and apply it to our image view
        ImageDownloader imageDownloader = new ImageDownloader();
        Bitmap celebImage;
        try
        {
            celebImage = imageDownloader.execute(celebUrls.get(chosenCeleb)).get();

            imageView.setImageBitmap(celebImage);
            //We get the correct answer from our array, as well as other options,
            // and we set the text for our buttons to match the names we got from the array
            locationOfCorrectAnswer = random.nextInt(4);
            int incorrectAnswerLocation;
            for (int i = 0; i < 4; i++)
            {
                if (i == locationOfCorrectAnswer)
                {
                    answers[i] = celebNames.get(chosenCeleb);
                } else
                {
                    incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    while (incorrectAnswerLocation == chosenCeleb)
                    {
                        incorrectAnswerLocation = random.nextInt(celebUrls.size());
                    }
                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
