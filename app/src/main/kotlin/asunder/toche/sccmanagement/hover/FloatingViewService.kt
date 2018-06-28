package asunder.toche.sccmanagement.hover

import android.R.attr.y
import android.R.attr.x
import android.app.Service
import android.view.View.OnTouchListener
import android.content.Intent
import android.widget.Toast
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.ImageView
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.main.ActivityMain
import asunder.toche.sccmanagement.preference.ROOT
import android.view.WindowManager
import android.os.Build




/**
 *Created by ToCHe on 2/6/2018 AD.
 */


class FloatingViewService : Service() {

    private var mWindowManager: WindowManager? = null
    private var mFloatingView: View? = null
    companion object {
        var currentContact: Model.Contact? = null

        fun updateCurrent(contact: Model.Contact){
            currentContact = contact
            println("Update CurrentContact $currentContact")
        }
    }


    /**
     * Detect if the floating view is collapsed or expanded.
     *
     * @return true if the floating view is collapsed.
     */
    private val isViewCollapsed: Boolean
        get() =
            mFloatingView == null || mFloatingView?.findViewById<View>(R.id.collapse_view)?.visibility == View.VISIBLE


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //Inflate the floating view layout we created
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)

        val layoutflag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutflag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)


        //Add the view to the window.
        /*
        val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
                */


        //Specify the view position
        params.gravity = Gravity.TOP or Gravity.LEFT        //Initially view will be added to top-left corner
        params.x = 0
        params.y = 150

        //Add the view to the window
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mFloatingView, params)

        //The root element of the collapsed view layout
        val collapsedView = mFloatingView!!.findViewById<View>(R.id.collapse_view)
        //The root element of the expanded view layout
        val expandedView = mFloatingView!!.findViewById<View>(R.id.expanded_container)

        //Set the close button
        val closeButtonCollapsed = mFloatingView!!.findViewById(R.id.close_btn) as ImageView
        closeButtonCollapsed.setOnClickListener {
            //close the service and remove the from from the window
            stopSelf()
        }

        //Set the view while floating view is expanded.
        //Set the play button.
        val playButton = mFloatingView!!.findViewById(R.id.play_btn) as ImageView
        playButton.setOnClickListener { Toast.makeText(this@FloatingViewService, "Playing the song.", Toast.LENGTH_LONG).show() }


        //Set the next button.
        val nextButton = mFloatingView!!.findViewById(R.id.next_btn) as ImageView
        nextButton.setOnClickListener { Toast.makeText(this@FloatingViewService, "Playing next song.", Toast.LENGTH_LONG).show() }


        //Set the pause button.
        val prevButton = mFloatingView!!.findViewById(R.id.prev_btn) as ImageView
        prevButton.setOnClickListener { Toast.makeText(this@FloatingViewService, "Playing previous song.", Toast.LENGTH_LONG).show() }


        //Set the close button
        val closeButton = mFloatingView!!.findViewById(R.id.close_button) as ImageView
        closeButton.setOnClickListener {
            collapsedView.visibility = View.VISIBLE
            expandedView.visibility = View.GONE
        }


        //Open the application on thi button click
        val openButton = mFloatingView!!.findViewById(R.id.open_button) as ImageView
        openButton.setOnClickListener {
            //Open the application  click.
            val intent = Intent(this@FloatingViewService, ActivityMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            //close the service and remove view from the view hierarchy
            stopSelf()
        }


        //Drag and move floating view using user's touch action.
        mFloatingView!!.findViewById<View>(R.id.root_container).setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()


            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {

                        //remember the initial position.
                        initialX = params.x
                        initialY = params.y

                        //get the touch location
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val Xdiff = (event.rawX - initialTouchX).toInt()
                        val Ydiff = (event.rawY - initialTouchY).toInt()


                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                //collapsedView.visibility = View.GONE
                                //expandedView.visibility = View.VISIBLE
                                Log.d("TOCHE","onTab")
                                currentContact?.let {
                                val intent = Intent()
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                intent.putExtra(ROOT.CONTACTS, currentContact)
                                startActivity(intent.setClass(this@FloatingViewService, ActivityMain::class.java))
                                }
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()


                        //Update the layout with new X & Y coordinate
                        mWindowManager!!.updateViewLayout(mFloatingView, params)
                        return true
                    }
                }
                return false
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mFloatingView != null) mWindowManager!!.removeView(mFloatingView)
    }
}