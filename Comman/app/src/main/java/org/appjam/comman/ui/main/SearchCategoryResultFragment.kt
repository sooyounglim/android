package org.appjam.comman.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.androidquery.AQuery
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_course_search_result.view.*
import kotlinx.android.synthetic.main.search_result_item.view.*
import org.appjam.comman.R
import org.appjam.comman.network.APIClient
import org.appjam.comman.network.data.CategoryData
import org.appjam.comman.util.ListUtils
import org.appjam.comman.util.PrefUtils
import org.appjam.comman.util.setDefaultThreads

/**
 * Created by ChoGyuJin on 2018-01-04.
 */
class SearchCategoryResultFragment : Fragment() {

    private var lecturesInfo : List<CategoryData.LecturesOfCategory> = listOf()
    private val disposables = CompositeDisposable()

    companion object {
        const val TAG = "SearchCategoryResultFragment"
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_course_search_result,container,false)
    }


    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val recyclerView = view!!.course_result_recyclerview
        recyclerView.adapter = CourseSearchResultAdapter()
        recyclerView.layoutManager = LinearLayoutManager(activity)

        if(arguments != null) {
            val categoryID = arguments.getInt("categoryID")
            disposables.add(APIClient.apiService.getLecturesOfCategory(PrefUtils.getUserToken(context), categoryID)
                    .setDefaultThreads()
                    .subscribe({
                        response -> lecturesInfo = response.result
                        recyclerView.adapter.notifyDataSetChanged()
                    }, {
                        failure -> Log.i(TAG, "on Failure ${failure.message}")
                    }))
        }

    }

    inner class ElemViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        fun bind(position: Int){
            val aQuery = AQuery(context)
            aQuery.id(itemView.course_result_img).image(lecturesInfo[position].image_path)
            itemView.course_title_tv.text = lecturesInfo[position].title
            itemView.course_content_tv.text = lecturesInfo[position].info
            val hit =lecturesInfo[position].hit
            itemView.course_people_tv.text = "$hit 명이 수강중입니다"
        }
    }
    inner class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class CourseSearchResultAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
            if(holder?.itemViewType == ListUtils.TYPE_FOOTER){
                holder as FooterViewHolder
            }
            else if(holder?.itemViewType == ListUtils.TYPE_HEADER){
                holder as HeaderViewHolder
            }
            else if (holder?.itemViewType == ListUtils.TYPE_ELEM){
                (holder as ElemViewHolder).bind(position-1)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == itemCount - 1 ) ListUtils.TYPE_FOOTER
            else if (position == 0) ListUtils.TYPE_HEADER
            else ListUtils.TYPE_ELEM
        }

        override fun getItemCount() = lecturesInfo.size + 1

        @SuppressLint("NewApi")
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
            return if(viewType == ListUtils.TYPE_FOOTER) {
                FooterViewHolder(layoutInflater.inflate(R.layout.course_item_footer, parent, false))
            }
            else if (viewType == ListUtils.TYPE_HEADER){
                HeaderViewHolder(layoutInflater.inflate(R.layout.category_header_item, parent, false))
            }
            else {
                ElemViewHolder(layoutInflater.inflate(R.layout.search_result_item, parent,false))
            }
        }
    }
}