# Dremio SingleStore ARP Connector

The SingleStore connector allows Dremio to connect to and query data in a SingleStore database. This can then allow you to build custom reports, dashboards, or even just ad-hoc SQL via your client tool of choice.


# ARP Overview

The Advanced Relational Pushdown (ARP) Framework allows for the creation of Dremio plugins for any data source which has a JDBC driver and accepts SQL as a query language. It allows for a mostly code-free creation of a plugin, allowing for modification of queries issued by Dremio using a configuration file.

There are three files that are necessary for creation of an ARP-based plugin:
- the storage plugin configuration, which is code (`SingleStoreConf.java`)
- the plugin ARP file, which is a YAML (https://yaml.org/) file (`singlestore-arp.yaml`)
- UI configuration (`singlestore-layout.json`)

The storage plugin configuration file tells Dremio what the name of the plugin should be, what connection options should be displayed in the source UI, what the name of the ARP file is, which JDBC driver to use and how to make a connection to the JDBC driver.

The ARP YAML file is what is used to modify the SQL queries that are sent to the JDBC driver, allowing you to specify support for different data types and functions, as well as rewrite them if tweaks need to be made for your specific data source.

## How to create your own custom ARP connector

Tutorial on ARP connectors: https://www.dremio.com/resources/tutorials/how-to-create-an-arp-connector/.

## ARP File Format

The ARP file is broken down into several sections:

**metadata**
- This section outlines some high level metadata about the plugin.

**syntax**
- This section allows for specifying some general syntax items like the identifier quote character.

**data_types**
- This section outlines which data types are supported by the plugin, their names as they appear in the JDBC driver, and how they map to Dremio types.

**relational_algebra** - This section is divided up into a number of other subsections:

- **aggregation**
  - Specify what aggregate functions, such as SUM, MAX, etc, are supported and what signatures they have. You can also specify a rewrite to alter the SQL for how this is issued.
- **except/project/join/sort/union/union_all/values**
  - These sections indicate if the specific operation is supported or not.
- **expressions**
  - This section outlines general operations that are supported. The main sections are:
- **operators**
  - Outlines which scalar functions, such as SIN, SUBSTR, LOWER, etc, are supported, along with the signatures of these functions which are supported. Finally, you can also specify a rewrite to alter the SQL for how this is issued.
- **variable_length_operators**
  - The same as operators, but allows specification of functions which may have a variable number of arguments, such as AND and OR.

If an operation or function is not specified in the ARP file, then Dremio will handle the operation itself. Any operations which are indicated as supported but need to be stacked on operations which are not will not be pushed down to the SQL query.


## Building and Installation

If you need to compile this plugin with another Dremio version, you need to update `pom.xml`: `version` and `properties->version.dremio` and run the steps below. This plugin should work with Dremio 17+.

1. In root directory with the `pom.xml` file run `mvn clean install`
2. Take the resulting .jar file from the target folder and put it to the `$DREMIO_HOME/jars` folder in Dremio
3. Download the [SingleStore JDBC driver](https://github.com/memsql/S2-JDBC-Connector/releases/download/v1.0.1/singlestore-jdbc-client-1.0.1.jar) and put in the `$DREMIO_HOME/jars/3rdparty` folder.
4. Restart Dremio.
