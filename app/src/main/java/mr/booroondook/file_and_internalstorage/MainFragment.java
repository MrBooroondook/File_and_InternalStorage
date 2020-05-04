package mr.booroondook.file_and_internalstorage;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class MainFragment extends Fragment {
    private static final String FILE_NAME = "note";

    private EditText editText;
    private File file;
    private LoadFileTask loadFileTask;
    private boolean isLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        file = new File(Objects.requireNonNull(getContext()).getFilesDir(), FILE_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        editText = view.findViewById(R.id.editNote);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isLoaded) {
            loadFileTask = new LoadFileTask();
            loadFileTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file);
        }
    }

    @Override
    public void onPause() {
        if (isLoaded) {
            new FileSaveThread(editText.getText().toString(), file).start();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (loadFileTask != null) {
            loadFileTask.cancel(false);
        }
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadFileTask extends AsyncTask<File, Void, String> {

        @Override
        protected String doInBackground(File... files) {
            String fileContent = "";
            if (files[0].exists()) {
                StringBuilder builder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(files[0]))) {
                    String s = reader.readLine();
                    while (s != null) {
                        builder.append(s);
                        builder.append("\n");
                        s = reader.readLine();
                    }
                    fileContent = builder.toString();
                } catch (IOException e) {
                    Log.e(getClass().getSimpleName(), "File reading exception", e);
                }
            }
            return fileContent;
        }

        @Override
        protected void onPostExecute(String s) {
            editText.setText(s);
            isLoaded = true;
            loadFileTask = null;
        }
    }

    private static class FileSaveThread extends Thread {
        private final String content;
        private final File file;

        FileSaveThread(String content, File file) {
            this.content = content;
            this.file = file;
        }

        @Override
        public void run() {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                writer.flush();
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "File writing Exception", e);
            }
        }
    }
}
