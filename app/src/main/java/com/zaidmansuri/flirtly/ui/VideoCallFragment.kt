package com.zaidmansuri.flirtly.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.zaidmansuri.flirtly.databinding.FragmentVideoCallBinding
import io.agora.base.internal.ContextUtils.getApplicationContext
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas


class VideoCallFragment : Fragment() {
    private lateinit var binding: FragmentVideoCallBinding
    private val appID = "65130da6619642e2b40367e8e9fdf8f3"
    private val channelName = "Flirtly"
    private val token =
        "007eJxTYPCpn9ryR8lN427L+TP1D3P0V3zSdVu2/GNY+ZWOiiCLH7MVGMxMDY0NUhLNzAwtzUyMUo2STAyMzcxTLVIt01LSLNKMM1vVUhoCGRkE53AwMzJAIIjPzuCWk1lUklPJwAAABukg5Q=="
    private val uid = 0
    private var isJoined = false
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null
    private var remoteSurfaceView: SurfaceView? = null

    fun showMessage(message: String?) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = requireContext()
            config.mAppId = appID
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoCallBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUESTED_PERMISSIONS,
                PERMISSION_REQ_ID
            )
        }
        setupVideoSDKEngine()

        binding.JoinButton.setOnClickListener {
            joinCall()
        }

        binding.LeaveButton.setOnClickListener {
            leaveCall()
        }
    }

    private fun leaveCall() {
        if (!isJoined) {
            showMessage("Join a channel first");
        } else {
            agoraEngine!!.leaveChannel();
            showMessage("You left the channel");
            // Stop remote video rendering.
            remoteSurfaceView?.setVisibility(View.GONE);
            // Stop local video rendering.
            localSurfaceView?.setVisibility(View.GONE);
            isJoined = false;
        }
    }

    private fun joinCall() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            // Start local preview.
            agoraEngine!!.startPreview()
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(
                getApplicationContext(),
                "Permissions was not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val PERMISSION_REQ_ID = 22;
    val REQUESTED_PERMISSIONS = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.CALL_PHONE
    )

    fun checkSelfPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                REQUESTED_PERMISSIONS[0]
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                REQUESTED_PERMISSIONS[1]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false;
        }
        return true;
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")
            requireActivity().runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined Channel $channel")
        }


        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            requireActivity().runOnUiThread{
                remoteSurfaceView!!.visibility = View.GONE
            }
        }
    }

    private fun setupRemoteVideo(uid: Int) {
        val container: FrameLayout = binding.remoteVideoViewContainer
        remoteSurfaceView = SurfaceView(requireContext())
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                uid
            )
        )
        remoteSurfaceView!!.visibility = View.VISIBLE
    }

    private fun setupLocalVideo() {
        val container: FrameLayout = binding.localVideoViewContainer
        localSurfaceView = SurfaceView(requireContext())
        container.addView(localSurfaceView)
        agoraEngine!!.setupLocalVideo(
            VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                0
            )
        )
        localSurfaceView!!.visibility = View.VISIBLE
    }

    private fun requestPermissions() {
        Dexter.withActivity(activity) // below line is use to request the number of
            // permissions which are required in our app.
            .withPermissions(
                 // below is the list of permissions
                Manifest.permission.CALL_PHONE,
            ) // after adding permissions we are
            // calling and with listener method.
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    // this method is called when all permissions are granted
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        Toast.makeText(
                            activity,
                            "All the permissions are granted..",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    permissionToken: PermissionToken?
                ) {
                    // this method is called when user grants some
                    // permission and denies some of them.
                    permissionToken!!.continuePermissionRequest()
                }
            }).withErrorListener { // we are displaying a toast message for error message.
                Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT)
                    .show()
            }
            .onSameThread().check()
    }

}