package com.xiaochi.player.ui.fragment

import android.Manifest
import android.content.AsyncQueryHandler
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.view.View
import com.xiaochi.player.R
import com.xiaochi.player.adapter.VbangAdapter
import com.xiaochi.player.base.BaseFragment
import com.xiaochi.player.model.AudioBean
import com.xiaochi.player.ui.activity.AudioPlayerActivity
import com.xiaochi.player.util.CursorUtil
import kotlinx.android.synthetic.main.fragment_vbang.*
import org.jetbrains.anko.noButton
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.yesButton


/**
 * ClassName:HomeFragment
 * Description:
 */
class VBangFragment : BaseFragment() {
    //    val handler = object :Handler(){
//        override fun handleMessage(msg: Message?) {
//            msg?.let {
//                val cursor = msg.obj as Cursor
//                //打印数据
//                CursorUtil.logCursor(cursor)
//            }
//        }
//    }
    override fun initView(): View? {
        return View.inflate(context, R.layout.fragment_vbang, null)
    }

    override fun initData() {
//        loadSongs()
        //动态权限申请
        handlePermission()
    }

    /**
     * 处理权限问题
     */
    private fun handlePermission() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        //查看是否有权限
        val checkSelfPermission = ActivityCompat.checkSelfPermission(context, permission)
        if(checkSelfPermission==PackageManager.PERMISSION_GRANTED){
            //已经获取
            loadSongs()
        }else{
            //没有获取权限
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity,permission)){
                //需要弹出
                alert("我们只会访问音乐文件,不会访问隐私照片", "温馨提示") {
                    yesButton { myRequestPermission() }
                    noButton {}
                }.show()
            }else{
                //不需要弹出
                myRequestPermission()
            }
        }
    }

    /**
     * 真正申请权限
     */
    private fun myRequestPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        requestPermissions(permissions,1)
    }

    /**
     * 接收权限授权结果
     * requestCode 请求码
     * permissions 权限申请数组
     * grantResults 申请之后结果
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
            loadSongs()
        }
    }

    private fun loadSongs() {
        //加载音乐列表数据
        val resolver = context.contentResolver
        //        val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        //                arrayOf(MediaStore.Audio.Media.DATA,
        //                        MediaStore.Audio.Media.SIZE,
        //                        MediaStore.Audio.Media.DISPLAY_NAME,
        //                        MediaStore.Audio.Media.ARTIST),
        //                null, null, null)
        //        //打印所有数据
        //        CursorUtil.logCursor(cursor)
        //开启线程查询音乐数据
        //        Thread(object : Runnable {
        //            override fun run() {
        //                val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        //                        arrayOf(MediaStore.Audio.Media.DATA,
        //                                MediaStore.Audio.Media.SIZE,
        //                                MediaStore.Audio.Media.DISPLAY_NAME,
        //                                MediaStore.Audio.Media.ARTIST),
        //                        null, null, null)
        //                val msg = Message.obtain()
        //                msg.obj = cursor
        //                handler.sendMessage(msg)
        //            }
        //        }).start()
        //asynctask
        //        AudioTask().execute(resolver)
        //
        val handler = object : AsyncQueryHandler(resolver) {
            override fun onQueryComplete(token: Int, cookie: Any?, cursor: Cursor?) {
                //查询完成回调  主线程中
                //打印数据
    //                CursorUtil.logCursor(cursor)
                //刷新列表
    //                (cookie as VbangAdapter).notifyDataSetChanged()
                //设置数据源
                //刷新adapter
                (cookie as VbangAdapter).swapCursor(cursor)
            }
        }

        //开始查询
        handler.startQuery(0, adapter, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.ARTIST),
                null, null, null)
    }

    var adapter: VbangAdapter? = null
    override fun initListener() {
        adapter = VbangAdapter(context, null)
        listView.adapter = adapter
        //设置条目点击事件
        listView.setOnItemClickListener { adapterView, view, i, l ->
            //获取数据集合
            val cursor = adapter?.getItem(i) as Cursor
            //通过当前位置cursor获取整个播放列表
            val list:ArrayList<AudioBean> = AudioBean.getAudioBeans(cursor)
            //位置position
            //跳转到音乐播放界面
            startActivity<AudioPlayerActivity>("list" to list,"position" to i)
        }
    }

    /**
     * 音乐查询异步任务
     */
    class AudioTask : AsyncTask<ContentResolver, Void, Cursor>() {

        /**
         * 后台执行任务  新线程
         */
        override fun doInBackground(vararg p0: ContentResolver?): Cursor? {
            val cursor = p0[0]?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.ARTIST),
                    null, null, null)
            return cursor
        }

        /**
         * 将后台任务结果回调到主线程中
         */
        override fun onPostExecute(result: Cursor?) {
            super.onPostExecute(result)
            //打印cursor
            CursorUtil.logCursor(result)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //界面销毁  关闭cursor
        //获取adapter中的cursor 关闭
        //将adapter中的cursor设置为null
        adapter?.changeCursor(null)
    }
}