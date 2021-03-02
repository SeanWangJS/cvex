package com.haswalk.cvex

import org.opencv.core.{Core, Mat, Size}
import org.opencv.imgproc.Imgproc

object CV {

  /*************************************************************************************************
   **************矩阵元素级二元运算函数族，特点是将原有API的输入型返回值修改为输出型返回值*******************
   *************************************************************************************************/

  def add(m1: Mat, m2: Mat): Mat = op(m1, m2, "add")

  def multiply(m1: Mat, m2: Mat): Mat = {
    op(m1, m2, "multiply")
  }

  def subtract(m1: Mat, m2: Mat): Mat = {
    op(m1, m2, "subtract")
  }

  private def op(m1: Mat, m2: Mat, name: String): Mat = {

    val method = classOf[Core].getDeclaredMethod(name, classOf[Mat], classOf[Mat], classOf[Mat])
    method.setAccessible(true)
    val result = new Mat()
    method.invoke(null, m1, m2, result)
    result

  }

  def resize(src: Mat, width: Int, height: Int, interpolation: Int): Mat = {
    val dst = new Mat
    Imgproc.resize(src, dst, new Size(width, height), 0, 0, interpolation)
    dst
  }

  def resize(src: Mat, scale: Double, interpolation: Int = Imgproc.INTER_AREA): Mat = {
    resize(src, (src.width() * scale).toInt, (src.height() * scale).toInt, interpolation)
  }
}
