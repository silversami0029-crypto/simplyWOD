package com.bessadi.fitwod;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    private Cursor cursor;
    private final Context context;
    private final MediaPlayer mediaPlayer;
    String voiceNote;

    private int mIdIndex;
    private int mNameIndex;
   // String voiceNote = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE));
    public WorkoutAdapter(Context mcontext, Cursor mcursor) {
        this.context = mcontext;
        this.cursor = mcursor;
        this.mediaPlayer = new MediaPlayer();


        // Only get column indices if cursor is not null
        if (mcursor != null) {
            mIdIndex = mcursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID);
            mNameIndex = mcursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE);
            this.voiceNote = mcursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE));

        }
    }
    // Add interface for delete callback
    public interface OnItemLongClickListener {
        void onItemLongClicked(int position, long workoutId);
    }
    private OnItemLongClickListener longClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_item_workout_history, parent, false);
        return new WorkoutViewHolder(view); // Correct initialization
    }
    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;

        if (newCursor != null) {
            try {
                mIdIndex = newCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID);
                mNameIndex = newCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE);
            } catch (IllegalArgumentException e) {
                Log.e("ADAPTER_ERROR", "Column not found in new cursor", e);
            }
        }
        notifyDataSetChanged(); // This will refresh ALL items
    }



    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        // First, move the cursor to the correct position
        if (!cursor.moveToPosition(position)) {
            Log.e("ADAPTER_ERROR", "Could not move cursor to position: " + position);
            return; // Exit if we can't move to the position
        }

        try {
            // Get workout ID first
            int idIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID);
            long workoutId = cursor.getLong(idIndex);

            // Get photo path
            int photoIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE);
            String photoPath = cursor.getString(photoIndex);

            // Get voice note path
            int voiceIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE);
            String voicePath = cursor.getString(voiceIndex);

            Log.d("FITWOD_DEBUG", "Adapter - Workout ID: " + workoutId +
                    ", Photo: '" + photoPath + "', Voice: '" + voicePath + "'");

            // Get other workout data
            String workType = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE));
            int work = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME));
            int rest = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME));
            int rounds = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS));
            long date = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));

            // Bind all data including voice and photo paths
            holder.bindData(workType, work, rest, rounds, date, voicePath, photoPath);

        } catch (IllegalArgumentException e) {
            Log.e("ADAPTER_ERROR", "Column not found in cursor", e);
        } catch (Exception e) {
            Log.e("ADAPTER_ERROR", "Error binding view holder: "+ e.getMessage(), e);
        }

    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    class WorkoutViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvParams, tvDate;
        private final ImageButton btnPlayVoice, btnTakePhoto;
        private final ImageView ivWorkoutType;

        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParams = itemView.findViewById(R.id.tvWorkoutParams);
            tvDate = itemView.findViewById(R.id.tvWorkoutDate);
            btnPlayVoice = itemView.findViewById(R.id.btnPlayVoice);
            btnTakePhoto = itemView.findViewById(R.id.btnTakePhoto);
            ivWorkoutType = itemView.findViewById(R.id.ivWorkoutType);
            // Set long click listener on the entire item view
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    // Get the workout ID from cursor
                    cursor.moveToPosition(position);
                    long workoutId = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID));
                    longClickListener.onItemLongClicked(position, workoutId);
                    return true;
                }
                return false;
            });

        }
        public void bindData(String workType,int work, int rest, int rounds, long dateMillis,
                             String voicePath, String photoPath) {
          //  String voiceNote = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE));
            // Set workout parameters and image
            Log.d("VIEWHOLDER_DEBUG", "bindData() - Photo path: " + photoPath);
            Log.d("VIEWHOLDER_DEBUG", "bindData() - Photo path not empty: " + (photoPath != null && !photoPath.isEmpty()));
            // Handle photos
            if (photoPath != null && !photoPath.isEmpty()) {
                Log.d("VIEWHOLDER_DEBUG", "Setting photo button VISIBLE");
                btnTakePhoto.setVisibility(VISIBLE);
                btnTakePhoto.setImageResource(R.drawable.ic_photo);

                // Test if file actually exists and is readable
                File photoFile = new File(photoPath);
                Log.d("VIEWHOLDER_DEBUG", "Photo file exists: " + photoFile.exists());
                Log.d("VIEWHOLDER_DEBUG", "Photo file readable: " + photoFile.canRead());
                Log.d("VIEWHOLDER_DEBUG", "Photo file size: " + photoFile.length());

                btnTakePhoto.setOnClickListener(v -> {
                    Log.d("VIEWHOLDER_DEBUG", "Photo button clicked for path: " + photoPath);
                    openPhoto(photoPath);
                });
            } else {
                Log.d("VIEWHOLDER_DEBUG", "Setting photo button GONE");
                btnTakePhoto.setVisibility(GONE);
                btnTakePhoto.setOnClickListener(null);
            }


            if ("TABATA".equals(workType)) {
                tvParams.setText(String.format(Locale.getDefault(),
                        workType + ": %dmin / %ds/ %ds", rounds, work, rest));
                ivWorkoutType.setImageResource(R.drawable.ic_tabata);

            } else if ("EMOM".equals(workType)) {
                tvParams.setText(String.format(Locale.getDefault(),
                        workType + ": %dmin for %dsec", rounds, work));
                ivWorkoutType.setImageResource(R.drawable.ice_emom);
            }
            else if ("AMRAP".equals(workType)) {
                tvParams.setText(String.format(Locale.getDefault(),
                        workType + ": %dsets in %dmin ", rest,rounds));
                ivWorkoutType.setImageResource(R.drawable.ice_amrap);
            }
            else if ("FOR TIME".equals(workType)) {
                tvParams.setText(String.format(Locale.getDefault(),
                        workType + ": %dsets in %dmin ", rest,rounds));
                ivWorkoutType.setImageResource(R.drawable.ice_fortime);
            }

            // Set date
            tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(dateMillis)));

            // Handle voice note: use voicePath (which is from COLUMN_VOICE_NOTE)
            if (voicePath != null && !voicePath.isEmpty()) {
                btnPlayVoice.setVisibility(VISIBLE);
                btnPlayVoice.setOnClickListener(v -> playMedia(voicePath));
            } else {
                btnPlayVoice.setVisibility(GONE);
            }

            // Handle photos: use videoPath (which is from COLUMN_VIDEO_NOTE)
            if (photoPath != null && !photoPath.isEmpty()) {
                btnTakePhoto.setVisibility(VISIBLE);
                btnTakePhoto.setImageResource(R.drawable.ic_photo); // Make the icon clickable to view photo
                btnTakePhoto.setOnClickListener(v -> {
                    openPhoto(photoPath);
                });


            } else {
                btnTakePhoto.setVisibility(GONE);
                btnTakePhoto.setOnClickListener(null);
            }

        }
        private void debugPhotoIssue(String photoPath) {
            Log.d("PHOTO_DEBUG", "Trying to open photo: " + photoPath);

            File photoFile = new File(photoPath);
            Log.d("PHOTO_DEBUG", "Photo file exists: " + photoFile.exists());
            Log.d("PHOTO_DEBUG", "Photo file size: " + photoFile.length() + " bytes");
            Log.d("PHOTO_DEBUG", "Photo file can read: " + photoFile.canRead());

            if (photoFile.exists()) {
                // Test if we can decode the image
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(photoPath, options);
                Log.d("PHOTO_DEBUG", "Image dimensions: " + options.outWidth + "x" + options.outHeight);
            }
        }

        public void openPhoto(String photoPath) {
            // Show photo in your own dialog
            //if (context == null) return;
            // Use itemView.getContext() instead of just 'context'
            if (itemView.getContext() == null) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photo_view, null);
            ImageView photoView = dialogView.findViewById(R.id.photo_view);

            // Load the photo efficiently
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // Reduce memory usage

              Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
                builder.setView(dialogView)
                        .setTitle("Workout Photo")
                        . setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                Toast.makeText(context, "Cannot load photo", Toast.LENGTH_SHORT).show();
            }
        }

        private void setupAudioPlayback(String audioPath) {
            // Button btnPlay = findViewById(R.id.btnPlayAudio);

            if (audioPath != null && !audioPath.isEmpty()) {
                btnPlayVoice.setVisibility(VISIBLE);
                btnPlayVoice.setOnClickListener(v -> playAudio(audioPath));
            } else {
                btnPlayVoice.setVisibility(GONE);
            }
        }
        private void playAudio(String path) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();

                // Update UI during playback
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    btnPlayVoice.setEnabled(true);
                });

                btnPlayVoice.setEnabled(false);
            } catch (IOException e) {
                Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
            }
        }

        private void playMedia(String path) {
            File file = new File(path);
            if (!file.exists()) {
                Toast.makeText(context, "Audio file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actual playback implementation
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e("PLAYBACK", "Error playing: " + path, e);
            }
        }

        private void playVideo(String path) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(path), "video/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);

        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mediaPlayer.release();
    }
}