package com.codepath.apps.tumblrsnap.activities;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.tumblrsnap.ImageFilterProcessor;
import com.codepath.apps.tumblrsnap.R;
import com.codepath.apps.tumblrsnap.TumblrClient;
import com.codepath.apps.tumblrsnap.models.User;
import com.codepath.libraries.androidviewhelpers.SimpleProgressDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class PreviewPhotoActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private Bitmap photoBitmap;
	private Bitmap processedBitmap;
	private SimpleProgressDialog dialog;
	private ImageView ivPreview;
	private ImageFilterProcessor filterProcessor;
	LocationClient mLocationClient;
	Location mLocation;
	private TextView tvLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview_photo);
		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		tvLocation = (TextView)findViewById(R.id.tvLocation);
		photoBitmap = getIntent().getParcelableExtra("photo_bitmap");
		filterProcessor = new ImageFilterProcessor(photoBitmap);
		redisplayPreview(ImageFilterProcessor.NONE);
		// Connect the location client to start receiving updates
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
	}

	private void redisplayPreview(int effectId) {
		if (mLocation != null) {
			String msg = "@ " + Double.toString(mLocation.getLatitude()) + ","
					+ Double.toString(mLocation.getLongitude());
			tvLocation.setText(msg);
		}
		processedBitmap = filterProcessor.applyFilter(effectId);
		ivPreview.setImageBitmap(processedBitmap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preview_photo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.more || itemId == R.id.action_save)
			return true;

		int effectId = 0;

		switch (itemId) {
		case R.id.filter_none:
			effectId = ImageFilterProcessor.NONE;
			break;
		case R.id.filter_blur:
			effectId = ImageFilterProcessor.BLUR;
			break;
		case R.id.filter_grayscale:
			effectId = ImageFilterProcessor.GRAYSCALE;
			break;
		case R.id.filter_crystallize:
			effectId = ImageFilterProcessor.CRYSTALLIZE;
			break;
		case R.id.filter_solarize:
			effectId = ImageFilterProcessor.SOLARIZE;
			break;
		case R.id.filter_glow:
			effectId = ImageFilterProcessor.GLOW;
			break;
		default:
			effectId = ImageFilterProcessor.NONE;
			break;
		}
		redisplayPreview(effectId);
		return true;
	}

	public void onSaveButton(MenuItem menuItem) {
		dialog = SimpleProgressDialog.build(this);
		dialog.show();

		TumblrClient client = ((TumblrClient) TumblrClient.getInstance(
				TumblrClient.class, this));
		client.createPhotoPost(User.currentUser().getBlogHostname(),
				processedBitmap, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int arg0, String arg1) {
						dialog.dismiss();
						PreviewPhotoActivity.this.finish();
					}

					@Override
					public void onFailure(Throwable arg0, String arg1) {
						dialog.dismiss();
					}
				});
	}

	public void onConnected(Bundle arg0) {
		mLocation = mLocationClient.getLastLocation();
		Log.d("DEBUG", "current location: " + mLocation.toString());
	}

	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
		String msg = "Updated Location: "
				+ Double.toString(location.getLatitude()) + ","
				+ Double.toString(location.getLongitude());
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub

	}
}
