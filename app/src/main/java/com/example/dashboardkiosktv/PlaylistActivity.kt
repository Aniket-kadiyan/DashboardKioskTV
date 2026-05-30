package com.example.dashboardkiosktv

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboardkiosktv.data.DashboardPage
import com.example.dashboardkiosktv.data.PlaylistParser
import com.example.dashboardkiosktv.data.PlaylistStorage

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistListView: ListView
    private lateinit var loopCheckbox: CheckBox
    private lateinit var addButton: Button
    private lateinit var editButton: Button
    private lateinit var deleteButton: Button
    private lateinit var moveUpButton: Button
    private lateinit var moveDownButton: Button
    private lateinit var saveButton: Button
    private lateinit var startButton: Button
    private lateinit var backButton: Button

    private lateinit var storage: PlaylistStorage
    private lateinit var adapter: ArrayAdapter<String>

    private val pages = mutableListOf<DashboardPage>()

    private var selectedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_playlist)

        storage = PlaylistStorage(this)

        playlistListView = findViewById(R.id.playlistListView)
        loopCheckbox = findViewById(R.id.loopCheckbox)
        addButton = findViewById(R.id.addButton)
        editButton = findViewById(R.id.editButton)
        deleteButton = findViewById(R.id.deleteButton)
        moveUpButton = findViewById(R.id.moveUpButton)
        moveDownButton = findViewById(R.id.moveDownButton)
        saveButton = findViewById(R.id.saveButton)
        startButton = findViewById(R.id.startButton)
        backButton = findViewById(R.id.backButton)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            mutableListOf()
        )

        playlistListView.adapter = adapter

        loadSavedPlaylist()
        setupListeners()
        refreshList()
    }

    private fun loadSavedPlaylist() {
        pages.clear()
        pages.addAll(
            PlaylistParser.parse(storage.getPlaylistTextOrDefault())
        )

        loopCheckbox.isChecked = storage.isLoopEnabled()
    }

    private fun setupListeners() {
        playlistListView.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position
            playlistListView.setItemChecked(position, true)
        }

        playlistListView.setOnItemLongClickListener { _, _, position, _ ->
            selectedIndex = position
            playlistListView.setItemChecked(position, true)
            openEditorForSelectedItem()
            true
        }

        addButton.setOnClickListener {
            openEditorForNewItem()
        }

        editButton.setOnClickListener {
            openEditorForSelectedItem()
        }

        deleteButton.setOnClickListener {
            deleteSelectedItem()
        }

        moveUpButton.setOnClickListener {
            moveSelectedItemUp()
        }

        moveDownButton.setOnClickListener {
            moveSelectedItemDown()
        }

        saveButton.setOnClickListener {
            savePlaylist(showToast = true)
        }

        startButton.setOnClickListener {
            saveAndStart()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun openEditorForNewItem() {
        val intent = Intent(this, DashboardPageEditorActivity::class.java)
        startActivityForResult(intent, REQUEST_ADD_ITEM)
    }

    private fun openEditorForSelectedItem() {
        val index = selectedIndex

        if (!isValidIndex(index)) {
            Toast.makeText(
                this,
                "Please select an item first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val page = pages[index]

        val intent = Intent(this, DashboardPageEditorActivity::class.java).apply {
            putExtra(DashboardPageEditorActivity.EXTRA_INDEX, index)
            putExtra(DashboardPageEditorActivity.EXTRA_TITLE, page.title)
            putExtra(DashboardPageEditorActivity.EXTRA_URL, page.url)
            putExtra(
                DashboardPageEditorActivity.EXTRA_DURATION_SECONDS,
                page.displaySeconds
            )
        }

        startActivityForResult(intent, REQUEST_EDIT_ITEM)
    }

    private fun deleteSelectedItem() {
        val index = selectedIndex

        if (!isValidIndex(index)) {
            Toast.makeText(
                this,
                "Please select an item first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val page = pages[index]

        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Delete '${page.title}' from playlist?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                pages.removeAt(index)
                selectedIndex = -1
                refreshList()
            }
            .show()
    }

    private fun moveSelectedItemUp() {
        val index = selectedIndex

        if (!isValidIndex(index)) {
            Toast.makeText(
                this,
                "Please select an item first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (index == 0) return

        val item = pages.removeAt(index)
        pages.add(index - 1, item)

        selectedIndex = index - 1
        refreshList()
    }

    private fun moveSelectedItemDown() {
        val index = selectedIndex

        if (!isValidIndex(index)) {
            Toast.makeText(
                this,
                "Please select an item first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (index == pages.lastIndex) return

        val item = pages.removeAt(index)
        pages.add(index + 1, item)

        selectedIndex = index + 1
        refreshList()
    }

    private fun saveAndStart() {
        if (!savePlaylist(showToast = false)) {
            return
        }

        startActivity(Intent(this, PlayerActivity::class.java))
        finish()
    }

    private fun savePlaylist(showToast: Boolean): Boolean {
        if (pages.isEmpty()) {
            Toast.makeText(
                this,
                "Please add at least one dashboard page.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val playlistText = PlaylistParser.toStorageText(pages)

        storage.savePlaylist(
            text = playlistText,
            loop = loopCheckbox.isChecked
        )

        if (showToast) {
            Toast.makeText(
                this,
                "Playlist saved.",
                Toast.LENGTH_SHORT
            ).show()
        }

        return true
    }

    private fun refreshList() {
        adapter.clear()

        if (pages.isEmpty()) {
            adapter.add("No dashboard pages added yet.")
        } else {
            adapter.addAll(
                pages.mapIndexed { index, page ->
                    "${index + 1}. ${page.title}\n${page.url}\n${page.displaySeconds} seconds"
                }
            )
        }

        adapter.notifyDataSetChanged()

        if (isValidIndex(selectedIndex)) {
            playlistListView.setItemChecked(selectedIndex, true)
        } else {
            playlistListView.clearChoices()
        }
    }

    private fun isValidIndex(index: Int): Boolean {
        return index >= 0 && index < pages.size
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }

        val title = data.getStringExtra(DashboardPageEditorActivity.EXTRA_TITLE)
            ?: "Dashboard"

        val url = data.getStringExtra(DashboardPageEditorActivity.EXTRA_URL)
            ?: return

        val duration = data.getLongExtra(
            DashboardPageEditorActivity.EXTRA_DURATION_SECONDS,
            30L
        )

        val newPage = DashboardPage(
            title = title,
            url = url,
            displaySeconds = duration
        )

        when (requestCode) {
            REQUEST_ADD_ITEM -> {
                pages.add(newPage)
                selectedIndex = pages.lastIndex
            }

            REQUEST_EDIT_ITEM -> {
                val index = data.getIntExtra(
                    DashboardPageEditorActivity.EXTRA_INDEX,
                    -1
                )

                if (isValidIndex(index)) {
                    pages[index] = newPage
                    selectedIndex = index
                }
            }
        }

        refreshList()
    }

    companion object {
        private const val REQUEST_ADD_ITEM = 1001
        private const val REQUEST_EDIT_ITEM = 1002
    }
}