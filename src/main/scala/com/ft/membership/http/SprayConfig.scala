package com.ft.membership.http

import java.net.URI

import scala.beans.BeanProperty

class SprayConfig(@BeanProperty() var connectionTimeout: Int,
                  @BeanProperty() var requestTimeout: Int,
                  @BeanProperty() var maxConnections: Int,
                  @BeanProperty() var baseUrl: URI)
