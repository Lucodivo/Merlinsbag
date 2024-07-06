package com.inasweaterpoorlyknit.core.ml

import android.content.Context
import android.util.Log
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions

fun encourageInstallSubjectSegmentationModel(context: Context) {
  val moduleInstallRequest = ModuleInstallRequest.newBuilder()
      .addApi(SubjectSegmentation.getClient(SubjectSegmenterOptions.Builder().build()))
      .build()
  ModuleInstall.getClient(context).installModules(moduleInstallRequest)
      .addOnSuccessListener {
        if (it.areModulesAlreadyInstalled()) {
          Log.i("ModuleInstall", "Subject Segmentation module was already installed.")
        } else {
          Log.i("ModuleInstall", "Subject Segmentation module has not yet been installed.")
        }
      }
      .addOnFailureListener {
        Log.e("ModuleInstall", "Subject Segmentation module failed to install")
      }

}