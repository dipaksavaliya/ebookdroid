package org.ebookdroid.ui.opds.adapters;

import org.ebookdroid.R;
import org.ebookdroid.common.cache.CacheManager;
import org.ebookdroid.common.cache.ThumbnailFile;
import org.ebookdroid.opds.Entry;
import org.ebookdroid.opds.Link;
import org.ebookdroid.opds.OPDSClient;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;

import org.emdev.ui.adapters.BaseViewHolder;
import org.emdev.ui.widget.TextViewMultilineEllipse;
import org.emdev.utils.LayoutUtils;
import org.emdev.utils.LengthUtils;

public class OPDSAdapter extends BaseAdapter {

    private static final List<Entry> emptyList = Collections.emptyList();

    public final Entry root;

    private final Context context;
    private final OPDSClient client;

    private Entry currentDirectory;
    private List<Entry> files = emptyList;

    public OPDSAdapter(final Context context, final String rootUri) {
        this.context = context;
        this.client = new OPDSClient();
        this.root = new Entry(new Link(rootUri));
        this.currentDirectory = root;
    }

    @Override
    public int getCount() {
        return LengthUtils.length(files);
    }

    @Override
    public Entry getItem(final int i) {
        if (LengthUtils.isNotEmpty(files)) {
            return files.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

    @Override
    public View getView(final int i, final View view, final ViewGroup parent) {

        final ViewHolder holder = BaseViewHolder.getOrCreateViewHolder(ViewHolder.class, R.layout.opdsitem, view,
                parent);

        final Entry file = getItem(i);

        holder.textView.setText(file.title);

        if (file.catalog != null) {
            holder.imageView.setImageResource(R.drawable.folderopen);
        } else {
            final ThumbnailFile thumbnailFile = CacheManager.getThumbnailFile(file.id);
            if (thumbnailFile.exists()) {
                holder.imageView.setImageBitmap(thumbnailFile.getRawImage());
            } else {
                holder.imageView.setImageResource(R.drawable.book);
            }
        }

        if (file.content != null) {
            String decoded = URLDecoder.decode(file.content.content);
            holder.info.setText(Html.fromHtml(decoded));
        } else {
            holder.info.setText("");
        }

        return holder.getView();
    }

    public void setCurrentDirectory(final Entry currentDirectory) {
        this.currentDirectory = currentDirectory;

        setFiles(emptyList);

        new LoadTask().execute(currentDirectory);
    }

    private void setFiles(final List<Entry> files) {
        this.files = files;
        notifyDataSetInvalidated();
    }

    public Entry getCurrentDirectory() {
        return currentDirectory;
    }

    public static class ViewHolder extends BaseViewHolder {

        TextView textView;
        ImageView imageView;
        TextViewMultilineEllipse info;

        @Override
        public void init(final View convertView) {
            super.init(convertView);
            textView = (TextView) convertView.findViewById(R.id.opdsItemText);
            imageView = (ImageView) convertView.findViewById(R.id.opdsItemIcon);
            info = (TextViewMultilineEllipse) convertView.findViewById(R.id.opdsDescription);
            info.setMaxLines(5);
            info.setTextSize(LayoutUtils.getDeviceSize(9));
            info.setTextColor(textView.getCurrentTextColor());
        }
    }

    final class LoadTask extends AsyncTask<Entry, String, List<Entry>> implements OnCancelListener {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            onProgressUpdate("Loading...");
        }

        @Override
        public void onCancel(final DialogInterface dialog) {
            this.cancel(true);
        }

        @Override
        protected List<Entry> doInBackground(final Entry... params) {
            List<Entry> entries = client.load(params[0]);
            for (Entry entry : entries) {
                loadBookThumbnail(entry);
            }
            return entries;
        }

        @Override
        protected void onPostExecute(final List<Entry> result) {
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                } catch (final Throwable th) {
                }
            }
            setFiles(result);
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            final int length = LengthUtils.length(values);
            if (length == 0) {
                return;
            }
            final String last = values[length - 1];
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(context, "", last, true);
                progressDialog.setCancelable(true);
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.setOnCancelListener(this);
            } else {
                progressDialog.setMessage(last);
            }
        }
    }

    public void loadBookThumbnail(final Entry entry) {
        if (entry.thumbnail == null) {
            return;
        }
        final ThumbnailFile thumbnailFile = CacheManager.getThumbnailFile(entry.id);
        if (thumbnailFile.exists()) {
            return;
        }

        try {
            File file = client.loadFile(entry.thumbnail);
            if (file == null) {
                return;
            }

            final Options opts = new Options();
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opts.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            opts.inSampleSize = getScale(opts, 200, 200);
            opts.inJustDecodeBounds = false;

            Bitmap image = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);
            thumbnailFile.setImage(image);
            image.recycle();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private int getScale(final Options opts, final float requiredWidth, final float requiredHeight) {
        int scale = 1;
        int widthTmp = opts.outWidth;
        int heightTmp = opts.outHeight;
        while (true) {
            if (widthTmp / 2 < requiredWidth || heightTmp / 2 < requiredHeight) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;

            scale *= 2;
        }
        return scale;
    }

}
