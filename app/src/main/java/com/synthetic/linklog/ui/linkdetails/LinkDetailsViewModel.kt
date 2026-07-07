package com.synthetic.linklog.ui.linkdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.synthetic.linklog.data.local.entity.Link
import com.synthetic.linklog.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LinkDetailsViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val linkId: Long = savedStateHandle.get<Long>("linkId") ?: 0L

    private val _link = MutableStateFlow<Link?>(null)
    val link = _link.asStateFlow()

    init {
        viewModelScope.launch {
            _link.value = linkRepository.getLinkById(linkId)
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            _link.value?.copy(notes = notes)?.let { updatedLink ->
                linkRepository.updateLink(updatedLink)
                _link.value = updatedLink
            }
        }
    }
}
