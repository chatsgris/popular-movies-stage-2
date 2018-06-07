package com.android.popularmoviesstage1.asynctasks;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;

public class FavoriteAsyncHandler extends AsyncQueryHandler {

    public FavoriteAsyncHandler(ContentResolver cr) {super(cr);}

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {}

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {}
}
