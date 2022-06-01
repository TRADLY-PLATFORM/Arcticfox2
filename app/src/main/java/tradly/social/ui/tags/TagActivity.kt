package tradly.social.ui.tags

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import tradly.social.R
import tradly.social.adapter.SuggestionAdapter
import tradly.social.domain.entities.AppError
import tradly.social.domain.entities.Tag
import tradly.social.common.base.BaseActivity
import kotlinx.android.synthetic.main.activity_tag.*
import kotlinx.android.synthetic.main.toolbar.*
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import tradly.social.common.util.parser.extension.toJson
import tradly.social.common.util.parser.extension.toList


class TagActivity : BaseActivity(), TagPresenter.View {

    private val tagList = mutableListOf<Tag>()
    private var tagPresenter: TagPresenter? = null
    private var suggestionAdapter: SuggestionAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tag)
        setToolbar(toolbar, R.string.tag_header_title, R.drawable.ic_back)
        suggestionAdapter = SuggestionAdapter(this, tagList) { position ->
            val selectedTag =  tagList[position]?.name
            edOpenValues.chipValues.let {
                it.add(selectedTag)
                edOpenValues.setText(it)
            }
        }
        tagPresenter = TagPresenter(this)
        suggestionList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        suggestionList.adapter = suggestionAdapter

        edOpenValues.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR)

        addTags(intent.getStringExtra("list"))

        edOpenValues.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(p0: Editable) {
                synchronized(this) {
                    if (p0.toString().isNotEmpty()) {
                        val tokens = edOpenValues.tokenValues
                        if (tokens.isNotEmpty()) {
                            val queryTxt = tokens[0].trim()
                            tagList.clear()
                            suggestionAdapter?.notifyDataSetChanged()
                            tagPresenter?.searchTags(queryTxt)
                        }
                        else{
                            tagList.clear()
                            suggestionAdapter?.notifyDataSetChanged()
                        }
                    } else {
                        tagList.clear()
                        suggestionAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })
    }

    private fun addTags(listString: String?) {
        val list = listString.toList<String>()
        edOpenValues.setText(list)
        edOpenValues.isFocusable = true
    }

    override fun showProgress() {
        progressBar?.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressBar?.visibility = View.GONE
    }

    override fun showTags(list: List<Tag>, keyword: String) {
        tagList.clear()
        tagList.addAll(list)
        suggestionAdapter?.notifyDataSetChanged()
    }

    override fun networkError() {

    }

    override fun onFailure(appError: AppError) {

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val saveItem = menu?.findItem(R.id.action_save)
        saveItem?.isVisible = true
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            val selectedTags = edOpenValues.chipValues
            val intent = Intent()
            intent.putExtra("list", selectedTags.toJson<List<String>>())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


}
