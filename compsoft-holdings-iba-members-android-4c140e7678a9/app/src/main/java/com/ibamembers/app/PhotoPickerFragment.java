package com.ibamembers.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.ibamembers.R;
import com.soundcloud.android.crop.Crop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class PhotoPickerFragment extends EventBusFragment {

    private static final String TAG = "PhotoPickerFragment";
    private static final int SCALED_IMAGE_HEIGHT = 100;
    private static final int SCALED_IMAGE_WIDTH = 75;

    protected static final int CAMERA_REQUEST = 0;
    protected static final int GALLERY_REQUEST = 1;

    private File selectedPhotoPath;
    String cameraPhotoPath;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped"));

            if (requestCode == CAMERA_REQUEST) {
                selectedPhotoPath = new File(cameraPhotoPath);
                Crop.of(Uri.fromFile(selectedPhotoPath), destination).withAspect(SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT).start(getActivity());
            } else if (requestCode == GALLERY_REQUEST) {
                Crop.of(data.getData(), destination).withAspect(SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT).start(getActivity());
            } else if (requestCode == Crop.REQUEST_CROP) {
                try {
                    File temp = File.createTempFile("iba", "image.png", getActivity().getExternalFilesDir(null));
                    selectedPhotoPath = temp;
                    saveBitmapContent(getActivity().getContentResolver(), Crop.getOutput(data), temp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void startAddPhotoIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, GALLERY_REQUEST);

        // Removes picker dialog to choose from camera or gallery due to specific Samsung camera intent issue.
//        android.support.v7.app.AlertDialog.Builder myAlertDialog = new android.suppuort.v7.app.AlertDialog.Builder(getActivity());
//        myAlertDialog.setMessage("Choose image from")
//                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//                        startActivityForResult(intent, GALLERY_REQUEST);
//                    }
//                })
//                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                            // Create the File where the photo should go
//                            File photoFile = null;
//                            try {
//                                photoFile = createImageFile();
//                            } catch (IOException ex) {
//                            }
//
//                            if (photoFile != null) {
//                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
//                            }
//                        }
//                    }
//                });
//        myAlertDialog.show();
    }

    private File createImageFile() throws IOException {
        File image = File.createTempFile("iba", "image.png", getActivity().getExternalFilesDir(null));
        cameraPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void imageResultComplete() {
        EventBus.getDefault().post(new ImageItemEvent(selectedPhotoPath));
    }

    public void imageResultError(Exception e) {
        e.printStackTrace();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ImageItemEvent imageItemEvent) {
        imagePicked(imageItemEvent.getImageIntent());
    }

    public void imagePicked(File file) {
    }

    /**
     * Save a bitmap picked from the image picker to the given file in a background thread.
     *
     * @param contentResolver The {@link ContentResolver}.
     * @param uri             The uri from the data intent from the {@link Activity#onActivityResult(int, int, Intent)}.
     * @param outputFile      The file to save the image to.
     */
    public void saveBitmapContent(final ContentResolver contentResolver, final Uri uri, final File outputFile) {
        new Thread() {
            @Override
            public void run() {
                File file = resizeBitmapFromFile(uri);

                if (file != null) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = contentResolver.openInputStream(Uri.parse("file://" + file.getAbsolutePath()));
                        out = new BufferedOutputStream(new FileOutputStream(outputFile));
                        copy(in, out);

                        imageResultComplete();
                    } catch (Exception e) {
                        imageResultError(e);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                            }
                        }
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }
            }
        }.start();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        out.flush();
        out.close();
        in.close();
    }

    /**
     * Load an image from the given file at a size that will equal or exceed the given bounds.
     * If the image contains any orientation exifs then adjust the rotation
     * Finally, resize the image and save to new location
     *
     * @param uri of image file
     * @return resized file
     */
    private File resizeBitmapFromFile(Uri uri) {
        File file = new File(uri.getPath());

        BitmapFactory.Options options = readImageBounds(file);
        options.inSampleSize = calculateInSampleSize(options, SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT);
        options.inJustDecodeBounds = false;

        int rotate = 0;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());

            int exifR = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            if (exifR == ExifInterface.ORIENTATION_ROTATE_90) {
                rotate = 90;
            } else if (exifR == ExifInterface.ORIENTATION_ROTATE_180) {
                rotate = 180;
            } else if (exifR == ExifInterface.ORIENTATION_ROTATE_270) {
                rotate = 270;
            }

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        Bitmap bitmap = null;
        int maxShrinkTries = 20;
        for (int i = 0; i < maxShrinkTries; i++) {
            try {
                // attempt to decode - may throw out of memory exception or
                // return null with no exception
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                if (bitmap == null) {
                    //reportThrowable(new Exception("Unable to decode bitmap"), null);
                    Log.e(TAG, "Unable to decode bitmap");
                } else if (rotate > 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotate);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
                if (bitmap == null) {
                    //reportThrowable(new Exception("Image post rotation failed"), null);
                    Log.e(TAG, "Image post rotation failed");
                }
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2; // Shrink the image
                if (i == maxShrinkTries - 1) {
                    reportThrowable(e, null);
                }
            } catch (Throwable t) {
                reportThrowable(t, null);
                break;
            }
        }

        if (bitmap != null) {
            bitmap = Bitmap.createScaledBitmap(bitmap, SCALED_IMAGE_WIDTH, SCALED_IMAGE_HEIGHT, false);

            if (saveBitmapToFile(bitmap)) {
                imageResultComplete();
            }
        }

        return null;
    }

    private boolean saveBitmapToFile(Bitmap bitmap) {
        try {
            FileOutputStream fOut = new FileOutputStream(selectedPhotoPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("PhotoPicker", "Save file error!");
            return false;
        }
    }

    public static BitmapFactory.Options readImageBounds(final File file) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        return options;
    }

    private static void reportThrowable(Throwable t, ThrowableListener listener) {
        if (listener != null) {
            listener.handleThrowable(PhotoPickerFragment.class, t);
        }
    }

    public interface ThrowableListener {
        /**
         * An exception has occurred which should be reported
         *
         * @param sender    The object in which the Exception arose.
         * @param throwable The exception.
         */
        void handleThrowable(Object sender, Throwable throwable);
    }

    /**
     * Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width larger than the requested height and width.
     *
     * @param options   The bitmap options (to get the actual height and width of the image)
     * @param reqWidth  The required width.
     * @param reqHeight The required height.
     * @return The number of samples to read in.
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize / 2;
    }

    public class ImageItemEvent {
        public File imageIntent;

        public ImageItemEvent(File intent) {
            this.imageIntent = intent;
        }

        public File getImageIntent() {
            return imageIntent;
        }

    }
}
