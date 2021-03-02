package com.haswalk.cvex

import java.awt.Color

import nu.pattern.OpenCV
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.scalatest.funsuite.AnyFunSuite

class MatUtilSuite extends AnyFunSuite with GetResources {

  OpenCV.loadLocally()

  test("add background to transparency image") {

    val path = getPath("imgs/input/pi.png")
    val src = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
    val result = MatUtil.addBackground(src, "#FF0000")
    val folder = getPath("imgs/output")
    Imgcodecs.imwrite(folder + "/pi.background.png", result)

  }

  test("compute histgram") {

    val path = getPath("imgs/input/lena.jpg")
    val src = Imgcodecs.imread(path)
    val array = MatUtil.histagram(src, bucket = 64, mode = 0)

    assert(array.length == 1)
    assert(array(0).length == 64)

  }

  test("test crop with polygon") {

    val path = getPath("imgs/input/lena.jpg")
    val src = Imgcodecs.imread(path)
    val result = MatUtil.ploygonCrop(src, Array(Array(0, 0), Array(100, 100), Array(200, 50)))
    val folder = getPath("imgs/output")
    Imgcodecs.imwrite(folder + "/lena.crop.png", result)


  }

  test("image rotate") {

    val path = getPath("imgs/input/lena.jpg")
    val src = Imgcodecs.imread(path)
    val result = MatUtil.rotate(src, 30, "#FFFFFF")
    val folder = getPath("imgs/output")
    Imgcodecs.imwrite(folder + "/lena.rotate.colorbg.png", result)

    val r2 = MatUtil.rotate(src, 30, "")
    Imgcodecs.imwrite(folder + "/lena.rotate.nobg.png", r2)

  }
}
