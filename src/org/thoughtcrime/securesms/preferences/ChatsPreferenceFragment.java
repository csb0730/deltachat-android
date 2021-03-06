package org.thoughtcrime.securesms.preferences;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.widget.Toast;

import com.b44t.messenger.DcContext;

import org.thoughtcrime.securesms.ApplicationPreferencesActivity;
import org.thoughtcrime.securesms.BlockedAndShareContactsActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.connect.DcHelper;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.util.Prefs;
import org.thoughtcrime.securesms.util.ScreenLockUtil;
import org.thoughtcrime.securesms.util.Util;

import static android.app.Activity.RESULT_OK;
import static org.thoughtcrime.securesms.connect.DcHelper.CONFIG_SHOW_EMAILS;

public class ChatsPreferenceFragment extends ListSummaryPreferenceFragment {
  private static final String TAG = ChatsPreferenceFragment.class.getSimpleName();


  ListPreference showEmails;
  CheckBoxPreference readReceiptsCheckbox;

//  CheckBoxPreference trimEnabledCheckbox;

  @Override
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);

    findPreference("pref_compression")
            .setOnPreferenceChangeListener(new ListSummaryListener());

    showEmails = (ListPreference) this.findPreference("pref_show_emails");
    showEmails.setOnPreferenceChangeListener((preference, newValue) -> {
      updateListSummary(preference, newValue);
      dcContext.setConfigInt(CONFIG_SHOW_EMAILS, Util.objectToInt(newValue));
      return true;
    });

    readReceiptsCheckbox = (CheckBoxPreference) this.findPreference("pref_read_receipts");
    readReceiptsCheckbox.setOnPreferenceChangeListener(new ReadReceiptToggleListener());

    this.findPreference("preference_category_blocked").setOnPreferenceClickListener(new BlockedContactsClickListener());

    Preference backup = this.findPreference("pref_backup");
    backup.setOnPreferenceClickListener(new BackupListener());

//    trimEnabledCheckbox = (CheckBoxPreference) findPreference("pref_trim_threads");
//    trimEnabledCheckbox.setOnPreferenceChangeListener(new TrimEnabledListener());
//
//    findPreference("pref_trim_length")
//        .setOnPreferenceChangeListener(new TrimLengthValidationListener());
//    findPreference("pref_trim_now")
//        .setOnPreferenceClickListener(new TrimNowClickListener());
  }

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.preferences_chats);
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity)getActivity()).getSupportActionBar().setTitle(R.string.pref_chats_and_media);

    initializeListSummary((ListPreference) findPreference("pref_compression"));

    String value = Integer.toString(dcContext.getConfigInt("show_emails"));
    showEmails.setValue(value);
    updateListSummary(showEmails, value);
    readReceiptsCheckbox.setChecked(0 != dcContext.getConfigInt("mdns_enabled", DcContext.DC_PREF_DEFAULT_MDNS_ENABLED));

//    trimEnabledCheckbox.setChecked(0!=dcContext.getConfigInt("trim_enabled", DcContext.DC_PREF_DEFAULT_TRIM_ENABLED));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CONFIRM_CREDENTIALS_BACKUP) {
      performBackup();
    } else {
      Toast.makeText(getActivity(), R.string.screenlock_authentication_failed, Toast.LENGTH_SHORT).show();
    }
  }

//  private class TrimEnabledListener implements Preference.OnPreferenceChangeListener {
//    @Override
//    public boolean onPreferenceChange(final Preference preference, Object newValue) {
//      boolean enabled = (Boolean) newValue;
//      dcContext.setConfigInt("trim_enabled", enabled? 1 : 0);
//      Toast.makeText(getActivity(), "Not yet implemented.", Toast.LENGTH_LONG).show();
//      return true;
//    }
//  }
//
//  private class TrimLengthValidationListener implements Preference.OnPreferenceChangeListener {
//
//    public TrimLengthValidationListener() {
//      EditTextPreference preference = (EditTextPreference)findPreference("pref_trim_length");
//      onPreferenceChange(preference, dcContext.getConfig("trim_length", ""+DcContext.DC_PREF_DEFAULT_TRIM_LENGTH));
//    }
//
//    @Override
//    public boolean onPreferenceChange(Preference preference, Object newValue) {
//      if (newValue == null || ((String)newValue).trim().length() == 0) {
//        return false;
//      }
//
//      int value;
//      try {
//        value = Integer.parseInt((String)newValue);
//      } catch (NumberFormatException nfe) {
//        Log.w(TAG, nfe);
//        return false;
//      }
//
//      if (value < 1) {
//        return false;
//      }
//
//      dcContext.setConfigInt("trim_length", value);
//      preference.setSummary(getResources().getString(R.string.pref_trim_length_limit_summary, value));
//      return true;
//    }
//  }
//
//  private class TrimNowClickListener implements Preference.OnPreferenceClickListener {
//    @Override
//    public boolean onPreferenceClick(Preference preference) {
//      final int threadLengthLimit = dcContext.getConfigInt("trim_length", DcContext.DC_PREF_DEFAULT_TRIM_LENGTH);
//      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//      builder.setMessage(getResources().getString(R.string.pref_trim_now_ask,
//          threadLengthLimit));
//      builder.setPositiveButton(R.string.ok,
//          (dialog, which) -> Toast.makeText(getActivity(), "Not yet implemented.", Toast.LENGTH_LONG).show());
//
//      builder.setNegativeButton(android.R.string.cancel, null);
//      builder.show();
//
//      return true;
//    }
//  }

  public static CharSequence getSummary(Context context) {
    final String onRes = context.getString(R.string.on);
    final String offRes = context.getString(R.string.off);
    String readReceiptState = DcHelper.getContext(context).getConfigInt("mdns_enabled", DcContext.DC_PREF_DEFAULT_MDNS_ENABLED)!=0? onRes : offRes;
    return context.getString(R.string.pref_read_receipts) + " " + readReceiptState;
  }

  private class BlockedContactsClickListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      Intent intent = new Intent(getActivity(), BlockedAndShareContactsActivity.class);
      intent.putExtra(BlockedAndShareContactsActivity.SHOW_ONLY_BLOCKED_EXTRA, true);
      startActivity(intent);
      return true;
    }
  }

  private class ReadReceiptToggleListener implements Preference.OnPreferenceChangeListener {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      boolean enabled = (boolean) newValue;
      dcContext.setConfigInt("mdns_enabled", enabled ? 1 : 0);
      return true;
    }
  }

  /***********************************************************************************************
   * Backup
   **********************************************************************************************/

  private class BackupListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(Preference preference) {
      boolean result = ScreenLockUtil.applyScreenLock(getActivity(), REQUEST_CODE_CONFIRM_CREDENTIALS_BACKUP);
      if (!result) {
        performBackup();
      }
      return true;
    }
  }

  private void performBackup() {
    Permissions.with(getActivity())
            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .ifNecessary()
            .withRationaleDialog(getActivity().getString(R.string.perm_explain_need_for_storage_access), R.drawable.ic_folder_white_48dp)
            .onAllGranted(() -> {
              new AlertDialog.Builder(getActivity())
                      .setTitle(R.string.pref_backup)
                      .setMessage(R.string.pref_backup_export_explain)
                      .setNegativeButton(android.R.string.cancel, null)
                      .setPositiveButton(R.string.pref_backup_export_start_button, (dialogInterface, i) -> startImex(DcContext.DC_IMEX_EXPORT_BACKUP))
                      .show();
            })
            .execute();
  }
}
