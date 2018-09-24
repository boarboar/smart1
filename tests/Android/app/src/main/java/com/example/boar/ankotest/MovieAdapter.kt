package com.example.boar.ankotest

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.AnkoContext

/**
 * Created by abduaziz on 5/25/18.
 */

class MovieAdapter(var list: ArrayList<Movie> = arrayListOf()) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return MovieViewHolder(MovieUI().createView(AnkoContext.create(parent.context, parent)))
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = list[position]
        holder.tvTitle.text = movie.title
        holder.tvYear.text = "${movie.year}"
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvTitle: TextView
        var tvYear: TextView

        init {
            tvTitle = itemView.findViewById(MovieUI.tvTitleId)
            tvYear = itemView.findViewById(MovieUI.tvYearId)
        }

    }
}
