package org.ebookdroid.common.notifications;

import android.content.Intent;

import org.emdev.common.android.AndroidVersion;

public interface INotificationManager {

    INotificationManager instance =
    /* OldestNotificationManager for Android 1.5 */
    AndroidVersion.VERSION == 3 ? new OldestNotificationManager() :
    /* CompatibilityNotificationManager for Android 1.6 - 2.x */
    new CompatibilityNotificationManager();

    int notify(final CharSequence title, final CharSequence message, final Intent intent);

    int notify(final CharSequence message);

    int notify(final int titleId, final CharSequence message, final Intent intent);

    int notify(final int titleId, final int messageId);

    int notify(final int messageId);

}
