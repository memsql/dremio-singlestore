/*
 * Copyright (C) 2017-2018 Dremio Corporation
 * Copyright (C) 2022 SingleStore, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.store.jdbc.conf;

import com.dremio.exec.catalog.conf.Property;

import com.dremio.options.OptionManager;
import com.dremio.security.CredentialsService;
import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.NotMetadataImpacting;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.store.jdbc.CloseableDataSource;
import com.dremio.exec.store.jdbc.DataSources;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.google.common.annotations.VisibleForTesting;

import io.protostuff.Tag;

import java.util.List;
import javax.validation.constraints.NotBlank;

/**
 * Configuration for SingleStore sources.
 */
@SourceType(value = "SINGLESTOREARP", label = "SingleStore", uiConfig = "singlestore-layout.json", externalQuerySupported = true)
public class SingleStoreConf extends AbstractArpConf<SingleStoreConf> {

  private static final String ARP_FILENAME = "arp/implementation/singlestore-arp.yaml";
  private static final ArpDialect ARP_DIALECT =
      AbstractArpConf.loadArpFile(ARP_FILENAME, (ArpDialect::new));
  private static final String DRIVER = "com.singlestore.jdbc.Driver";

  @NotBlank
  @Tag(1)
  @DisplayMetadata(label = "Host")
  public String host;

  @Tag(2)
  @DisplayMetadata(label = "Port")
  public int port = 3306;
  
  @NotBlank
  @Tag(3)
  @DisplayMetadata(label = "Database")
  public String database;
  
  @NotBlank
  @Tag(4)
  @DisplayMetadata(label = "Username")
  public String user;

  @NotBlank
  @Tag(5)
  @Secret
  @DisplayMetadata(label = "Password")
  public String password;

  @Tag(6)
  @DisplayMetadata(label = "Use SSL")
  public boolean useSsl;

  @Tag(7)
  @DisplayMetadata(label = "Server Certificate")
  public String serverSslCert;

  @Tag(8)
  @DisplayMetadata(label = "Record fetch size")
  @NotMetadataImpacting
  public int fetchSize = 200;

  @Tag(9)
  @DisplayMetadata(label = "Maximum idle connections")
  @NotMetadataImpacting
  public int maxIdleConns = 8;

  @Tag(10)
  @DisplayMetadata(label = "Connection idle time (s)")
  @NotMetadataImpacting
  public int idleTimeSec = 60;

  @Tag(11)
  public List<Property> propertyList;

  @VisibleForTesting
  public String toJdbcConnectionString() {
    String url =  String.format("jdbc:singlestore://%s:%s", host, port);
    boolean userProvided = false;
    if (database != null && !database.equals("")) {
      url += String.format("/%s", database);
    }
    if (user != null && !user.equals("")) {
      url += String.format("?user=%s", user);
      userProvided = true;
    }
    if (password != null && !password.equals("")) {
      url += String.format("%spassword=%s", userProvided ? "&" : "?", password);
    }
    if (useSsl) {
      url += "&useSsl=true";
    }
    if (serverSslCert != null && !serverSslCert.equals("")) {
      url += String.format("&serverSslCert=%s", serverSslCert);
    }
    if (this.propertyList != null) {
      for (Property p : this.propertyList) {
        url += String.format("&%s=%s", p.name, p.value);
      }
    }

    return url;
  }

  @Override
  @VisibleForTesting
  public JdbcPluginConfig buildPluginConfig(
          JdbcPluginConfig.Builder configBuilder,
          CredentialsService credentialsService,
          OptionManager optionManager
  ) {
    return configBuilder.withDialect(getDialect())
            .withDialect(getDialect())
            .withFetchSize(fetchSize)
            .withDatasourceFactory(this::newDataSource)
            .clearHiddenSchemas()
            .addHiddenSchema("SYSTEM")
            .build();
  }

  private CloseableDataSource newDataSource() {
    return DataSources.newGenericConnectionPoolDataSource(DRIVER,
      toJdbcConnectionString(), user, password, null, DataSources.CommitMode.DRIVER_SPECIFIED_COMMIT_MODE,
            maxIdleConns, idleTimeSec);
  }

  @Override
  public ArpDialect getDialect() {
    return ARP_DIALECT;
  }

  @VisibleForTesting
  public static ArpDialect getDialectSingleton() {
    return ARP_DIALECT;
  }
}
