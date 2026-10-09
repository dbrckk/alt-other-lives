package com.alt.otherlives.core.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Test-APK-only provider. Deliberately uses only Java/Android runtime APIs:
 * the isolated test APK provider process does not load the Kotlin stdlib from
 * the target application, so a Kotlin ContentProvider would crash on startup.
 */
public final class SlowPhotoTestProvider extends ContentProvider {
    public static final String AUTHORITY = "com.alt.otherlives.test.slowphotos";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return "image/jpeg";
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        String streamMode = uri.getLastPathSegment();
        if (!"r".equals(mode) ||
                (!"slow-jpeg".equals(streamMode) && !"oversized".equals(streamMode))) {
            throw new FileNotFoundException("Unknown or non-readable test stream");
        }

        final ParcelFileDescriptor[] pipe;
        try {
            pipe = ParcelFileDescriptor.createPipe();
        } catch (IOException error) {
            FileNotFoundException failure = new FileNotFoundException(
                    "Unable to create test pipe"
            );
            failure.initCause(error);
            throw failure;
        }

        final ParcelFileDescriptor writer = pipe[1];
        Thread pump = new Thread(() -> {
            try (OutputStream output = new ParcelFileDescriptor.AutoCloseOutputStream(writer)) {
                if ("slow-jpeg".equals(streamMode)) {
                    byte[] jpeg = createFixtureJpeg();
                    int initial = Math.min(1024, jpeg.length / 2);
                    output.write(jpeg, 0, initial);
                    output.flush();
                    // Deliberately stall a real ContentResolver pipe read.
                    Thread.sleep(1_400L);
                    output.write(jpeg, initial, jpeg.length - initial);
                } else {
                    byte[] block = new byte[64 * 1024];
                    Arrays.fill(block, (byte) 0x53);
                    for (int i = 0; i < 51 * 1024 * 1024 / block.length; i++) {
                        output.write(block);
                    }
                }
            } catch (IOException expectedClosedPipe) {
                // The importer closes the read end on cancellation or byte limit.
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            }
        }, "alt-slow-photo-test-stream");
        pump.setDaemon(true);
        pump.start();
        return pipe[0];
    }

    private static byte[] createFixtureJpeg() throws IOException {
        Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, buffer)) {
                throw new IOException("Unable to encode test JPEG");
            }
            return buffer.toByteArray();
        } finally {
            bitmap.recycle();
        }
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder
    ) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs
    ) {
        return 0;
    }
}
