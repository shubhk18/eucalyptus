/*******************************************************************************
 *Copyright (c) 2009 Eucalyptus Systems, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 * 
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Please contact Eucalyptus Systems, Inc., 130 Castilian
 * Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 * if you need additional information or have any questions.
 * 
 * This file may incorporate work covered under the following copyright and
 * permission notice:
 * 
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2008, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 * THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 * LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 * SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 * IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 * BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 * THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 * OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 * WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 * ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
/*
 * Author: chris grzegorczyk <grze@eucalyptus.com>
 */
package com.eucalyptus.cluster.callback;

import org.apache.log4j.Logger;
import com.eucalyptus.cluster.Clusters;
import com.eucalyptus.component.ServiceConfigurations;
import com.eucalyptus.component.Topology;
import com.eucalyptus.entities.Transactions;
import com.eucalyptus.network.ExtantNetwork;
import com.eucalyptus.network.NetworkGroup;
import com.eucalyptus.network.NetworkGroups;
import com.eucalyptus.network.NetworkToken;
import com.eucalyptus.util.LogUtil;
import com.eucalyptus.util.Logs;
import com.eucalyptus.util.NotEnoughResourcesAvailable;
import com.eucalyptus.util.async.BroadcastCallback;
import com.eucalyptus.util.async.Callback;
import com.google.common.collect.Lists;
import edu.ucsb.eucalyptus.msgs.StartNetworkResponseType;
import edu.ucsb.eucalyptus.msgs.StartNetworkType;

public class StartNetworkCallback extends BroadcastCallback<StartNetworkType, StartNetworkResponseType> {
  
  private static Logger      LOG = Logger.getLogger( StartNetworkCallback.class );
  
  private final NetworkGroup networkGroup;
  private final Integer      tag;
  
  public StartNetworkCallback( final NetworkGroup networkGroup ) {
    this.networkGroup = networkGroup;
    Integer ttag = -1;
    try {
      ttag = networkGroup.extantNetwork( ).getTag( );
    } catch ( NotEnoughResourcesAvailable ex ) {
      Logs.extreme( ).error( ex, ex );
    }
    this.tag = ttag;
    StartNetworkType msg = new StartNetworkType( networkGroup.getOwnerUserId( ),
                                                 this.tag,
                                                 networkGroup.getNaturalId( ),
                                                 networkGroup.getNaturalId( ) );
    this.setRequest( msg );
  }
  
  @Override
  public void fire( StartNetworkResponseType msg ) {
    try {
      Transactions.naturalId( this.networkGroup, new Callback<NetworkGroup>( ) {
        
        @Override
        public void fire( NetworkGroup input ) {
          input.setState( NetworkGroup.State.ACTIVE );
        }
      } );
    } catch ( Throwable e ) {
      LOG.debug( e, e );
    }
  }
  
  @Override
  public void initialize( StartNetworkType msg ) {
    try {
      msg.setNameserver( edu.ucsb.eucalyptus.cloud.entities.SystemConfiguration.getSystemConfiguration( ).getNameserverAddress( ) );
      msg.setClusterControllers( Lists.newArrayList( Clusters.getInstance( ).getClusterAddresses( ) ) );
    } catch ( Throwable e ) {
      LOG.debug( e, e );
    }
  }
  
  @Override
  public void fireException( Throwable e ) {
    LOG.debug( LogUtil.subheader( this.getRequest( ).toString( "eucalyptus_ucsb_edu" ) ) );
    LOG.debug( e, e );
  }
  
  @Override
  public BroadcastCallback<StartNetworkType, StartNetworkResponseType> newInstance( ) {
    return new StartNetworkCallback( this.networkGroup );
  }
  
}
