package com.cipherxzc.clockinapp.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.clockinapp.data.ClockInItem
import com.cipherxzc.clockinapp.ui.LocalClockInItemDao
import com.cipherxzc.clockinapp.ui.LocalCurrentUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AddItemDialog(
    itemsState: ClockInStatus,
    showDialogState: MutableState<Boolean>
){
    val clockInItemDao = LocalClockInItemDao.current
    val currentUser = LocalCurrentUser.current

    var newItemName by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val userId = currentUser.uid

    // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
    if (showDialogState.value) {
        AlertDialog(
            onDismissRequest = { showDialogState.value = false },
            shape = MaterialTheme.shapes.large, // 使用主题形状
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("添加新的打卡项") },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Name") },
                        placeholder = { Text("Enter item name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        value = newItemDescription,
                        onValueChange = { newItemDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Enter description") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 当用户输入的 name 非空时，进行添加操作
                        if (newItemName.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val newItem = ClockInItem(
                                    name = newItemName,
                                    userId = userId,
                                    description = if (newItemDescription.isBlank()) null else newItemDescription
                                )
                                clockInItemDao.insert(newItem)

                                withContext(Dispatchers.Main) {
                                    itemsState.unClockedInItems.value = itemsState.unClockedInItems.value + newItem
                                    // 清空输入，并关闭对话框
                                    newItemName = ""
                                    newItemDescription = ""
                                    showDialogState.value = false
                                }
                            }
                        } else {
                            // 可选：可以给用户提示名称为必填项
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 取消操作，清空输入并关闭对话框
                        newItemName = ""
                        newItemDescription = ""
                        showDialogState.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
