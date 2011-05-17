package com.eucalyptus.webui.client;

import com.eucalyptus.webui.client.activity.AccountActivity;
import com.eucalyptus.webui.client.activity.ErrorSinkActivity;
import com.eucalyptus.webui.client.activity.LogoutActivity;
import com.eucalyptus.webui.client.activity.ConfigActivity;
import com.eucalyptus.webui.client.activity.StartActivity;
import com.eucalyptus.webui.client.activity.VmTypeActivity;
import com.eucalyptus.webui.client.place.AccountPlace;
import com.eucalyptus.webui.client.place.ErrorSinkPlace;
import com.eucalyptus.webui.client.place.LogoutPlace;
import com.eucalyptus.webui.client.place.ConfigPlace;
import com.eucalyptus.webui.client.place.StartPlace;
import com.eucalyptus.webui.client.place.VmTypePlace;
import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class MainActivityMapper implements ActivityMapper {
  
  private ClientFactory clientFactory;
  
  public MainActivityMapper( ClientFactory clientFactory ) {
    super( );
    this.clientFactory = clientFactory;
  }
  
  @Override
  public Activity getActivity( Place place ) {
    if ( place instanceof StartPlace ) {
      return new StartActivity( ( StartPlace )place, this.clientFactory );
    } else if ( place instanceof ConfigPlace ) {
      return new ConfigActivity( ( ConfigPlace )place, this.clientFactory );
    } else if ( place instanceof ErrorSinkPlace ) {
      return new ErrorSinkActivity( ( ErrorSinkPlace )place, this.clientFactory );
    } else if ( place instanceof LogoutPlace ) {
      return new LogoutActivity( ( LogoutPlace )place, this.clientFactory );
    } else if ( place instanceof AccountPlace ) {
      return new AccountActivity( ( AccountPlace )place, this.clientFactory );
    } else if ( place instanceof VmTypePlace ) {
      return new VmTypeActivity( ( VmTypePlace )place, this.clientFactory );
    }
    return null;
  }
  
}
