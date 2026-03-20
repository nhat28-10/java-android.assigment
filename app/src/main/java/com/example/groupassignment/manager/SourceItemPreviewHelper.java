package com.example.groupassignment.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Size;
import android.webkit.MimeTypeMap;

import com.example.groupassignment.manager.model.DatasetSourceItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class SourceItemPreviewHelper {

    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "webp", "gif"
    ));

    private SourceItemPreviewHelper() {
    }

    public static String resolveDisplayName(Context context, Uri uri) {
        if (context == null || uri == null) {
            return "source-item";
        }
        try (android.database.Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.DISPLAY_NAME},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String value = cursor.getString(index);
                    if (!TextUtils.isEmpty(value)) {
                        return value;
                    }
                }
            }
        } catch (Exception ignored) {
            // Fall back to uri parsing below.
        }

        String lastSegment = uri.getLastPathSegment();
        return TextUtils.isEmpty(lastSegment) ? "source-item" : lastSegment;
    }

    public static long resolveSizeBytes(Context context, Uri uri) {
        if (context == null || uri == null) {
            return -1L;
        }
        try (android.database.Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{OpenableColumns.SIZE},
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception ignored) {
            // Some providers do not expose file size.
        }
        return -1L;
    }

    public static String resolveMimeType(Context context, Uri uri, String displayName, String fallbackDatasetType) {
        String mimeType = null;
        if (context != null && uri != null) {
            mimeType = context.getContentResolver().getType(uri);
        }
        if (!TextUtils.isEmpty(mimeType)) {
            return mimeType;
        }

        String extension = getNormalizedExtension(displayName, uri == null ? null : uri.toString());
        if (!TextUtils.isEmpty(extension)) {
            String guessed = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (!TextUtils.isEmpty(guessed)) {
                return guessed;
            }
            if (IMAGE_EXTENSIONS.contains(extension)) {
                return "image/" + extension;
            }
        }

        if ("audio".equalsIgnoreCase(fallbackDatasetType)) {
            return "audio/*";
        }
        if ("text".equalsIgnoreCase(fallbackDatasetType)) {
            return "text/plain";
        }
        return "image/*";
    }

    public static boolean isImage(DatasetSourceItem item) {
        return item != null && isImage(item.getMimeType(), item.getItemName(), item.getItemPathOrContent());
    }

    public static boolean isAudio(DatasetSourceItem item) {
        return item != null && isAudio(item.getMimeType(), item.getItemName(), item.getItemPathOrContent());
    }

    public static boolean isText(DatasetSourceItem item) {
        return item != null && isText(item.getMimeType(), item.getItemName(), item.getItemPathOrContent());
    }

    public static boolean isImage(String mimeType, String displayName, String uriString) {
        if (!TextUtils.isEmpty(mimeType) && mimeType.toLowerCase(Locale.getDefault()).startsWith("image/")) {
            return true;
        }
        String extension = getNormalizedExtension(displayName, uriString);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    public static boolean isAudio(String mimeType, String displayName, String uriString) {
        if (!TextUtils.isEmpty(mimeType) && mimeType.toLowerCase(Locale.getDefault()).startsWith("audio/")) {
            return true;
        }
        String extension = getNormalizedExtension(displayName, uriString);
        return Arrays.asList("mp3", "wav", "m4a", "aac", "ogg", "flac").contains(extension);
    }

    public static boolean isText(String mimeType, String displayName, String uriString) {
        if (!TextUtils.isEmpty(mimeType)) {
            String normalized = mimeType.toLowerCase(Locale.getDefault());
            if (normalized.startsWith("text/")) {
                return true;
            }
            if (normalized.contains("json") || normalized.contains("xml") || normalized.contains("csv")) {
                return true;
            }
        }
        String extension = getNormalizedExtension(displayName, uriString);
        return Arrays.asList("txt", "json", "xml", "csv", "log", "md").contains(extension);
    }

    public static Bitmap loadImageThumbnail(Context context, String uriString, int requestedSizePx) {
        if (context == null || TextUtils.isEmpty(uriString)) {
            return null;
        }
        Uri uri = Uri.parse(uriString);
        try {
            return context.getContentResolver().loadThumbnail(uri, new Size(requestedSizePx, requestedSizePx), null);
        } catch (Exception ignored) {
            // Continue to sampled decode.
        }

        try (InputStream boundsStream = context.getContentResolver().openInputStream(uri)) {
            if (boundsStream == null) {
                return null;
            }
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(boundsStream, null, bounds);

            BitmapFactory.Options decode = new BitmapFactory.Options();
            decode.inPreferredConfig = Bitmap.Config.RGB_565;
            decode.inSampleSize = calculateInSampleSize(bounds, requestedSizePx, requestedSizePx);

            try (InputStream decodeStream = context.getContentResolver().openInputStream(uri)) {
                if (decodeStream == null) {
                    return null;
                }
                return BitmapFactory.decodeStream(decodeStream, null, decode);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    public static String readTextPreview(Context context, String uriString, int maxChars) {
        if (context == null || TextUtils.isEmpty(uriString)) {
            return "";
        }
        Uri uri = Uri.parse(uriString);
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             InputStreamReader streamReader = inputStream == null ? null : new InputStreamReader(inputStream);
             BufferedReader reader = streamReader == null ? null : new BufferedReader(streamReader)) {
            if (reader == null) {
                return "";
            }
            char[] buffer = new char[256];
            int remaining = maxChars;
            while (remaining > 0) {
                int read = reader.read(buffer, 0, Math.min(buffer.length, remaining));
                if (read < 0) {
                    break;
                }
                builder.append(buffer, 0, read);
                remaining -= read;
            }
        } catch (IOException ignored) {
            return "";
        }
        return normalizePreviewText(builder.toString());
    }

    public static String formatFileSize(long sizeBytes) {
        if (sizeBytes < 0) {
            return "Unknown size";
        }
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }
        double size = sizeBytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unitIndex = -1;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024.0;
            unitIndex++;
        }
        return String.format(Locale.getDefault(), "%.1f %s", size, units[Math.max(unitIndex, 0)]);
    }

    public static String buildSecondaryMetadata(DatasetSourceItem item) {
        if (item == null) {
            return "";
        }
        String mime = TextUtils.isEmpty(item.getMimeType()) ? "Unknown type" : item.getMimeType();
        String size = formatFileSize(item.getSizeBytes());
        return mime + " • " + size;
    }

    public static String normalizePreviewText(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        while ((height / inSampleSize) > reqHeight * 2 || (width / inSampleSize) > reqWidth * 2) {
            inSampleSize *= 2;
        }
        return Math.max(1, inSampleSize);
    }

    private static String getNormalizedExtension(String displayName, String uriString) {
        String candidate = !TextUtils.isEmpty(displayName) ? displayName : uriString;
        if (TextUtils.isEmpty(candidate)) {
            return "";
        }
        int queryIndex = candidate.indexOf('?');
        if (queryIndex >= 0) {
            candidate = candidate.substring(0, queryIndex);
        }
        int dotIndex = candidate.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex >= candidate.length() - 1) {
            return "";
        }
        return candidate.substring(dotIndex + 1).toLowerCase(Locale.getDefault());
    }
}
