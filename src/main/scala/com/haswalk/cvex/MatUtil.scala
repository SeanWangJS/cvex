package com.haswalk.cvex

import java.awt.Color
import java.util
import java.util.function.Consumer

import com.haswalk.cvex.CVContext._
import org.opencv.core._
import org.opencv.imgproc.Imgproc

/**
 * 基于OpenCV二次开发的Mat变换工具类
 * */
object MatUtil {

  /**
   * Mat 资源释放方法
   * */
  def release(mats: Mat*): Unit = {
    mats.foreach(_.release())
  }

  /**
   * 为背景透明的图片添加纯色背景
   *
   * @param src 原图
   * @param color 16进制颜色码
   * */
  def addBackground(src: Mat, color: String): Mat = {

    if(src.channels() < 4) {
      return src
    }

    val rgb = Color.decode(color)
    val scalar = new Scalar(rgb.getBlue, rgb.getGreen, rgb.getRed)
    val background = new Mat(src.height(), src.width(), src.`type`(), scalar)

    val src1 = new util.ArrayList[Mat]()
    val src2 = new util.ArrayList[Mat]()

    Core.split(src, src1)
    Core.split(background, src2)
    val alpha = src1.get(3)
    val beta = Mat.ones(alpha.height(), alpha.width(), alpha.`type`()) - alpha

    // dst = src1 * alpha + src2 * (1 - alpha)
    val c1 = (src1.get(0) * alpha) + (src2.get(0) * beta)
    val c2 = (src1.get(1) * alpha) + (src2.get(1) * beta)
    val c3 = (src1.get(2) * alpha) + (src2.get(2) * beta)

    val result = new Mat()

    Core.merge(util.Arrays.asList(c1, c2, c3), result)

    src1.forEach((m: Mat) => m.release())
    src2.forEach(m => m.release())
    release(background, alpha, beta, c1, c2, c3, result)

    result
  }

  /**
   * 计算图片的灰度直方图
   * @param src 原图
   * @param bucket 直方图数量
   * @param mode 计算模式，0-灰度直方图，1-RGB直方图
   * @return 二维数组中的每个子数组为对应通道的直方图，灰度模式只有一个通道
   * */
  def histagram(src: Mat, bucket: Int = 256, mode: Int = 0): Array[Array[Float]] = {

    val mat = if(mode == 0) {
      val mat = new Mat
      Imgproc.cvtColor(src, mat, Imgproc.COLOR_BGR2GRAY)
      mat
    } else {
      src.clone()
    }

    val images = new util.ArrayList[Mat]()
    Core.split(mat, images)
    val histSize = new MatOfInt(bucket)
    val histRange = new MatOfFloat(0, 256)

    val hist = (0 until images.size())
      .map(i => {
        val channel = new MatOfInt(i)
        val histagram = new Mat()
        Imgproc.calcHist(images, channel, new Mat(), histagram, histSize, histRange, false)
        val arr = Array.fill[Float](bucket)(0)
        histagram.get(0, 0, arr)
        release(channel, histagram)
        arr
      })
      .toArray

    release(mat, histRange, histSize)
    images.forEach(_.release())

    hist

  }

  /**
   * 使用多边形裁切图片
   * 原理步骤；
   * 1.
   * @param src 原图
   * @param polygon 多边形
   * */
  def ploygonCrop(src: Mat, polygon: Array[Array[Int]]): Mat = {

    val bgraSrc = new Mat()
    Imgproc.cvtColor(src, bgraSrc, Imgproc.COLOR_BGR2BGRA)

    val mask = Mat.zeros(bgraSrc.size(), CvType.CV_8UC1)
    val result = Mat.zeros(bgraSrc.size(), bgraSrc.`type`())

    val contours = new util.ArrayList[MatOfPoint]()
    val points = polygon.map(p => new Point(p(0), p(1)))
    val contour = new MatOfPoint()
    contour.fromArray(points:_*)
    contours.add(contour)
    Imgproc.drawContours(mask, contours, 0, new Scalar(255, 255, 255, 255), -1)

    bgraSrc.copyTo(result, mask)

    release(bgraSrc, mask, contour)

    result
  }

  /**
   * 图像旋转方法
   * @see <a href="https://seanwangjs.github.io/2020/03/27/opencv-image-process-rotate.html">原理步骤</a>
   * @param src 原图
   * @param angle 逆时针旋转角度，360度制
   * @param color 图片旋转后空白部分的填充颜色，16进制颜色码，若为空，则背景透明
   * */
  def rotate(src: Mat, angle: Double, color: String): Mat = {

    var c: Color = null

    val mat = if((color == null || color.isEmpty) && src.channels() < 4) { // 如果没有指定背景颜色，采用透明背景
      val m = new Mat()
      Imgproc.cvtColor(src, m, Imgproc.COLOR_BGR2BGRA)
      c = new Color(0, 0, 0, 0)
      m
    } else if(color != null && !color.isEmpty && src.channels() == 4){ // 如果背景颜色正常，但图片为4通道，则转换为3通道
      val m = new Mat()
      Imgproc.cvtColor(src, m, Imgproc.COLOR_BGRA2BGR)
      c = Color.decode(color)
      m
    } else {
      c = Color.decode(color)
      src.clone()
    }

    val r = ((360 - angle) % 360) / 360.0 * 2 * Math.PI

    // 旋转后图片的长宽
    val dstWidth = (Math.abs(mat.width * Math.cos(r)) + Math.abs(mat.height * Math.sin(r))).toInt
    val dstHeight = (Math.abs(mat.width * Math.sin(r)) + Math.abs(mat.height * Math.cos(r))).toInt

    // 旋转前后图片缩小比例
    val scale = Math.min(mat.height / dstHeight.toDouble, mat.width / dstWidth.toDouble)
    // 为了不让旋转后图片缩小，先进行放大
    val resizedSrc = CV.resize(mat, 1 / scale)

    // 获得旋转矩阵
    val center = new Point(resizedSrc.width / 2.0, resizedSrc.height / 2.0)
    val rotationMatrix2D = Imgproc.getRotationMatrix2D(center, angle, scale)

    val dst = new Mat
    // 应用仿射变换
    Imgproc.warpAffine(resizedSrc, dst, rotationMatrix2D, resizedSrc.size, Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(c.getRed, c.getGreen, c.getBlue, 0))

    // 裁掉多余的部分
    val left = Math.max(0, dst.width / 2 - dstWidth / 2)
    val top = Math.max(0, dst.height / 2 - dstHeight / 2)
    val right = Math.min(dst.width, dst.width / 2 + dstWidth / 2)
    val bottom = Math.min(dst.height, dst.height / 2 + dstHeight / 2)
    val submat = dst.submat(top, bottom, left, right)

    release(mat, resizedSrc, rotationMatrix2D, dst)

    submat

  }

  /**
   * 透明图片内容描边
   * @see <a href="https://seanwangjs.github.io/2020/03/28/opencv-image-process-stroke.html">原理步骤</a>
   * @param src 原图
   * @param color 16进制颜色码
   * @param thick 描边宽度
   * */
  def stroke(src: Mat, color: String, thick: Int): Mat = {

    if(src.channels() < 4) {
      return src
    }

    // 分离alpha通道
    val channels = new util.ArrayList[Mat]()
    Core.split(src, channels)
    val alpha = channels.get(3)

    // alpha 通道 dilate
    Imgproc.dilate(alpha, alpha, Mat.ones(thick, thick, CvType.CV_32S))

    // 为原始图片加上背景色
    val bg = addBackground(src, color)

    // 以新的 alpha 为 mask，将 bg 拷贝生成临时图片
    val temp = Mat.zeros(src.size(), src.`type`())
    bg.copyTo(temp, alpha)

    // 转换 alpha 的类型，并作为 temp 的透明度通道
    val channels2 = new util.ArrayList[Mat]()
    Core.split(temp, channels2)
    alpha.convertTo(alpha, channels2.get(0).`type`())
    Core.split(bg, channels2)
    channels2.add(alpha)

    // 合并 channel2，生成描边图
    val result = new Mat()
    Core.merge(channels2, result)

    // 把result 转换成 src 一样的类型
    result.convertTo(result, src.`type`())

    // 释放资源
    channels.forEach((t: Mat) => t.release())
    channels2.forEach((t: Mat) => t.release())
    release(alpha, temp, bg)

    result
  }

}
