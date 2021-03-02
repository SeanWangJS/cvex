package com.haswalk.cvex

import java.util

import nu.pattern.OpenCV
import org.opencv.core.{CvType, Mat}
import org.opencv.imgcodecs.Imgcodecs
import org.scalatest.funsuite.AnyFunSuite

class CVSuite extends AnyFunSuite {

  OpenCV.loadLocally()
  import com.haswalk.cvex.CVContext._

  test("add mat and mat") {

    val m1 = Mat.ones(3, 3, CvType.CV_8U)
    val m2 = Mat.ones(3, 3, CvType.CV_8U)

    val result = m1 + m2

    assert(result.get(0, 0)(0) == 2)

  }

  test("mat element-wise multiply") {
    val m1 = Mat.ones(3, 3, CvType.CV_8U)
    val m2 = Mat.ones(3, 3, CvType.CV_8U)

    val result = m1 * m2
    assert(result.get(0, 0)(0) == 1)

  }

  test("") {

    val mat = Imgcodecs.imread("D:/data/image/lena.jpg")
    val sub = mat.submat(0, 3, 0, 3)
    println(sub.dump())

  }
}
