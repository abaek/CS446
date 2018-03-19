package com.cs446.articleshare.fragments;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446.articleshare.R;
import com.cs446.articleshare.tasks.ParseHtmlAsyncTask;
import com.cs446.articleshare.tasks.Value;
import com.cs446.articleshare.tasks.WebPages;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import static com.cs446.articleshare.Util.getTextFromClipboard;

public class SourcePickerFragment extends Fragment {
    private ClipboardManager clipboard;
    ProgressBar spinner;
    ScrollView content;
    RadioGroup sourceGroup;

    List<AsyncTask> tasks;
    List<Source> sources;

    private OnSourceUpdateListener mListener;
    public SourcePickerFragment() {
        // Required empty public constructor
    }

    public static int title() {
        return R.string.tab_source;
    }

    public static SourcePickerFragment newInstance() {
        SourcePickerFragment fragment = new SourcePickerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onSourceUpdated(JsonObject searchResults, Error error) {
        if (error != null) {
            // TODO show error
            return;
        }

        if (searchResults != null) {
            JsonObject webPagesJson = searchResults.getAsJsonObject("webPages");
            WebPages webPages = new Gson().fromJson(
                    new JsonParser().parse(webPagesJson.toString()),
                    WebPages.class
            );

            if (webPages.getValue().isEmpty()) {
                // TODO show no results error
                return;
            }

            for (int i = 0; i < webPages.getValue().size(); i++) {
                final Value webPage = webPages.getValue().get(i);
                ParseHtmlAsyncTask titleTask = new ParseHtmlAsyncTask(webPage.getUrl(),
                    new ParseHtmlAsyncTask.Callback() {
                        @Override
                        public void onComplete(Object o, Error error) {
                            Source source = constructSource(o, error, webPage);
                            sources.add(source);
                            int buttonIndex = sources.size() - 1;
                            RadioButton rb = getRadioButton(source, buttonIndex);
                            sourceGroup.addView(rb);
                            if (buttonIndex == 0) sourceGroup.check(0);

                            spinner.setVisibility(View.GONE);
                            content.setVisibility(View.VISIBLE);
                        }
                    });
                tasks.add(titleTask);
                titleTask.execute();
            }
        }

        if (sources.isEmpty()){
            spinner.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);
        } else {
            spinner.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        }
    }

    private Source constructSource(Object o, Error error, Value webPage) {
        String title = webPage.getName();
        if (error == null && ((String) o).length() > 0) {
            title = (String) o;
        }
        return new Source(title, getPrettyUrl(webPage.getDisplayUrl()), webPage.getUrl());
    }


    private String getPrettyUrl(String url) {
        String baseUrl = url;
        if(baseUrl.startsWith("https://www.")){
            baseUrl = baseUrl.substring("https://www.".length());
        }else if(baseUrl.startsWith("http://www.")){
            baseUrl = baseUrl.substring("http://www.".length());
        }else if(baseUrl.startsWith("https://")){
            baseUrl = baseUrl.substring("https://".length());
        }else if(baseUrl.startsWith("http://")){
            baseUrl = baseUrl.substring("http://".length());
        }
        int backslashAt = baseUrl.indexOf('/');
        if(backslashAt > 0){
            baseUrl = baseUrl.substring(0, backslashAt);
        }
        return baseUrl;
    }

    private RadioButton getRadioButton(Source source, int index) {
        RadioButton rb = new RadioButton(getActivity());
        String html;
        if(source.getTitle() == null || source.getTitle().isEmpty()) {
            html = source.getDisplayUrl();
        }else{
            html = "<b>" + source.getTitle() + "</b> - " + source.getDisplayUrl();
        }
        rb.setId(index);
        rb.setText(Html.fromHtml(html), TextView.BufferType.SPANNABLE);
        return rb;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        tasks = new ArrayList<>();
        sources = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View layout = inflater.inflate(
                R.layout.fragment_source_picker,
                container,
                false
        );

        spinner = (ProgressBar) layout.findViewById(R.id.spinner);
        content = (ScrollView) layout.findViewById(R.id.content);
        sourceGroup = (RadioGroup) layout.findViewById(R.id.source_options);
        Button pasteButton = (Button) layout.findViewById(R.id.paste_source_button);

        pasteButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String pasteData = getTextFromClipboard(getActivity(), clipboard);
                if(pasteData != null){
                    useCustomUrl(pasteData, sourceGroup);
                }else{
                    Toast.makeText(getActivity(), getString(R.string.empty_clipboard_toast),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        sourceGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId >= 0) mListener.onSourceSelected(sources.get(checkedId));
            }
        });

        return layout;
    }

    private void useCustomUrl(final String url, final RadioGroup sourceSelect) {
        // check if text is URL
        ParseHtmlAsyncTask titleTask =
                new ParseHtmlAsyncTask(url, new ParseHtmlAsyncTask.Callback(){
                    @Override
                    public void onComplete(Object o, Error error) {
                        if(error != null){
                            Toast.makeText(getActivity(), "The clipboard text is not a valid URL.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String title = (String) o;

                        String baseUrl = getPrettyUrl(url);

                        Source customSource = new Source(title, baseUrl, url);
                        if(sourceSelect != null) sourceSelect.clearCheck();
                        mListener.onSourceSelected(customSource);
                    }
                });
        tasks.add(titleTask);
        titleTask.execute();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSourceUpdateListener) {
            mListener = (OnSourceUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSourceUpdateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(AsyncTask task : tasks){
            task.cancel(true);
        }
    }

    public interface OnSourceUpdateListener {
        void onSourceSelected(Source source);
    }
}

