package com.inasweaterpoorlyknit.merlinsbag.ui.screen

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.inasweaterpoorlyknit.merlinsbag.ui.toast

@Composable
fun Toast(@StringRes msg: Int) = LocalContext.current.toast(msg)

// It seems navigation string arguements between routes are encoded/decoded in a way that
// causes a permission denial when accessing URIs. And only in release builds.
// Problem came to light reading this Stack Overflow post
// Source: "Passing uri between compose screens causes: Security Exception: Permission Denial"
// Answered by: Phil Dukhov
// Link: https://stackoverflow.com/questions/72122868/passing-uri-between-compose-screens-causes-securityexception-permission-denial
fun navigationSafeUriStringEncode(uri: Uri) = Uri.encode(uri.toString().replace("%", "|"))
fun navigationSafeUriStringDecode(uriString: String) = uriString.replace("|", "%")