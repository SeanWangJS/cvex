package com.haswalk.cvex

trait GetResources {

  def getPath(name: String): String = {

    this.getClass.getResource("/" + name).getPath.toString.substring(1)

  }

}
