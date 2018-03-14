package asunder.toche.sccmanagement.auth

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import asunder.toche.sccmanagement.Model
import asunder.toche.sccmanagement.R
import asunder.toche.sccmanagement.auth.adapter.ManageAdapter
import asunder.toche.sccmanagement.custom.RecyclerLoadMore
import asunder.toche.sccmanagement.custom.extension.hideLoading
import asunder.toche.sccmanagement.custom.extension.hideRefresh
import asunder.toche.sccmanagement.custom.extension.showLoading
import asunder.toche.sccmanagement.preference.ROOT
import asunder.toche.sccmanagement.service.ManageUserService
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_management.*

/**
 *Created by ToCHe on 10/3/2018 AD.
 */
class ActivityManagement : AppCompatActivity(),ManageUserService.ServiceState{

    private var loadMoreListener: RecyclerLoadMore? = null
    private val TAG = this::class.java.simpleName
    private lateinit var manageAdapter: ManageAdapter
    lateinit var service : ManageUserService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_management)
        service = ManageUserService()
        initRv()
        setRefreshListener()
        fetchUsers()

    }

    private fun initRv(){
        manageAdapter = ManageAdapter()
        manageAdapter.setCallBack(this)
        rvUsers.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@ActivityManagement)
            adapter = manageAdapter
        }
    }

    private fun setRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener {
            loadMoreListener?.resetState()
            manageAdapter.clear()
            fetchUsers()
        }
    }

    private fun fetchUsers(){
        service.fetchUsers(this@ActivityManagement)
        loading.showLoading()
    }

    override fun loadUserSuccess(users: MutableList<Model.UserAuth>) {
        manageAdapter.setUserAuth(users)
        loading.hideLoading()
        swipeRefreshLayout.hideRefresh()
        Paper.book().write(ROOT.MANAGEMENT,Model.ManagementUser(users))

    }

    override fun onApprove(userAuth: Model.UserAuth) {
        service.approveUser(userAuth)
        Snackbar.make(rvUsers,"Approve ${userAuth.email}",Snackbar.LENGTH_LONG).show()
        upDateDb(ROOT.APPROVE,userAuth.uid)

    }

    override fun onReject(userAuth: Model.UserAuth) {
        service.rejectUser(userAuth)
        Snackbar.make(rvUsers,"Reject ${userAuth.email}",Snackbar.LENGTH_LONG).show()
        upDateDb(ROOT.APPROVE,userAuth.uid)
    }

    override fun onTerminate(userAuth: Model.UserAuth) {
        service.terminateUser(userAuth)
        Snackbar.make(rvUsers,"Terminate ${userAuth.email}",Snackbar.LENGTH_LONG).show()
        upDateDb(ROOT.APPROVE,userAuth.uid)
    }

    fun upDateDb(status:String,uid: String){
        manageAdapter.usersAuth
                .filter { it.uid == uid }
                .forEach { it.status_user = status }
        Paper.book().write(ROOT.MANAGEMENT,Model.ManagementUser(manageAdapter.usersAuth))
        manageAdapter.updateUsers()
    }
}