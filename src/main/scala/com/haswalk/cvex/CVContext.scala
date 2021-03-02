package com.haswalk.cvex

import org.opencv.core.Mat

object CVContext {

  implicit class add(m1: Mat) {
    def + (m2: Mat): Mat = CV.add(m1, m2)
  }

  implicit class multiply(m1: Mat) {
    def * (m2: Mat): Mat = CV.multiply(m1, m2)
  }

  implicit class subtract(m1: Mat) {
    def - (m2: Mat): Mat = CV.subtract(m1, m2)
  }
}
