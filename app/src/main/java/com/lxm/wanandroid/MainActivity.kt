package com.lxm.wanandroid

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.lxm.module_library.helper.okhttp.cookies.CookieManger
import com.lxm.module_library.statusbar.StatusBarUtil
import com.lxm.module_library.utils.AppUtils.context
import com.lxm.module_library.utils.PreferencesUtil
import com.lxm.wanandroid.repository.remote.LoginRepository
import com.lxm.wanandroid.ui.*
import com.lxm.wanandroid.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.main_content.*


const val PHOTO_URL = "https://cdn.duitang.com/uploads/blog/201404/22/20140422142715_8GtUk.thumb.600_0.jpeg"

class MainActivity : AppCompatActivity() {

    private val mTitles by lazy {
        mutableListOf("主页", "知识体系", "导航", "项目", "福利")
    }
    private val mFragments: MutableList<Fragment> by lazy {
        mutableListOf<Fragment>()
    }

    private val mAdapter: MyPagerAdapter by lazy {
        MyPagerAdapter(supportFragmentManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StatusBarUtil.setColor(this, resources.getColor(R.color.colorTheme), 0)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        iv_photo.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        var loginViewModel = ViewModelProviders.of(this@MainActivity, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LoginViewModel(LoginRepository()) as T
            }

        })[LoginViewModel::class.java]

        var textView = navigationView.getHeaderView(0).findViewById<TextView>(R.id.tvUserName)
        textView.text = "萧兮易水寒"
        val menuItem = navigationView.menu.findItem(R.id.accout) as MenuItem
        var isLogin: Boolean by PreferencesUtil<Boolean>("login", false)
        if (isLogin) {
            menuItem.title = "注销"
            menuItem.setIcon(R.drawable.ic_logout_menu)
        }else{
            menuItem.title = "登陆"
            menuItem.setIcon(R.drawable.ic_login_menu)
        }
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.colletion -> {
                    if (isLogin) {
                        val intent = Intent(this@MainActivity, CollectActivity::class.java)
                        startActivity(intent)
                    } else {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }

                }
                R.id.about -> {

                }
                R.id.accout->{
                    if (isLogin) {
                        loginViewModel.logoutStatus.observe(this@MainActivity, Observer {
                            if(it?.errorCode ==0){
                                //退出成功 清空cookie
                                isLogin = false
                               CookieManger.clearAllCookies()
                            }
                        })
                    } else {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
                else -> {

                }
            }
            drawerLayout.closeDrawer(Gravity.START)
            true
        }


        mFragments.let {
            it.add(ArticleFragment.getInstance())
            it.add(TreeFragment.getInstance())
            it.add(NavigationFragment.getInstance())
            it.add(ProjectFragment.getInstance())
            it.add(WelfareFragment.getInstance())
        }

        mAdapter.setFragments(mFragments)
        mAdapter.setTitles(mTitles)
        view_pager.adapter = mAdapter
        view_pager.offscreenPageLimit = 5
        tablayout.setViewPager(view_pager)


    }

    private fun showPhoto(imageView: ImageView) {
        Glide.with(this@MainActivity)
            .load(PHOTO_URL)
            .transition(DrawableTransitionOptions.withCrossFade(500))
            .transform(CircleCrop())
            .into(iv_photo)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawer(Gravity.START)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}

class MyPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    var fragmentList: MutableList<Fragment> = mutableListOf()
    var titleList: MutableList<String> = mutableListOf()

    override fun getCount(): Int {
        return fragmentList.size
    }

    override fun getItem(p0: Int): Fragment {
        return fragmentList[p0]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }

    fun setFragments(fragment: MutableList<Fragment>) {
        fragmentList = fragment;
    }

    fun setTitles(mTitles: MutableList<String>) {
        titleList = mTitles
    }
}

