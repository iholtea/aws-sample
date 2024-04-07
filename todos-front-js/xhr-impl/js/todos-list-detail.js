import todosXhr from './todos-xhr.js'
import globalData from './global-data.js'

const itemEditModal = new bootstrap.Modal(document.getElementById('item-edit-modal'));

// this might be just fetchItemsByListId
function fetchList(listUuid) {
  const todoList = globalData.todos.get(listUuid);
  if( todoList.items.size==0 ) {
    console.log( `Display one list: fetching items ${todoList.title}` );
    todosXhr.fetchListById(listUuid, fetchListCallback);
  } else {
    console.log( `Display one list: items already fetched ${todoList.title}` );
    renderList(todoList);
  }
}

function fetchListCallback(err, data) {
  if(!err) {
    parseItemsByList(data);
    renderList( globalData.todos.get(globalData.currentTodoUuid) );
  } else {
    console.log('Error calling backed service');
  }  
}

function parseItemsByList(receivedData) {
  const receivedTodo = JSON.parse(receivedData);
  const cachedTodo = globalData.todos.get( globalData.currentTodoUuid );
  receivedTodo.items.forEach( receivedItem => {
    const todoItem = {};
    todoItem.uuid = receivedItem.uuid;
    todoItem.listUuid = receivedItem.listUuid;
    todoItem.text = receivedItem.text;
    todoItem.done = receivedItem.done;
    todoItem.orderIdx = receivedItem.orderIdx;
    todoItem.extraInfo = receivedItem.extraInfo;
    cachedTodo.items.set( todoItem.uuid, todoItem );
  });
}

function renderList(todo) {
  
  const mainContainer = document.getElementById('main-container');
  mainContainer.innerHTML = '';

  mainContainer.appendChild( renderListTitle(todo) );

  // Iteration over Maps is always in insertion order
  // items aldread sorted by order index from the backend 
  // so no need to sort the map.values() which is the items collection
  todo.items.forEach( (item,itemKey) => {
    
    const rowDiv = document.createElement('div');
    rowDiv.classList.add('row');
    rowDiv.appendChild( renderItemData(item) );
    rowDiv.appendChild( renderItemActionCol(item) );
    mainContainer.appendChild(rowDiv);
  
  });

  mainContainer.appendChild( renderNewItem() );  

  bindItemCheckActions();
  bindItemDeleteActions();
  bindItemEditActions();
  
}

function renderListTitle(todo) {
  const topRow = document.createElement('div');
  topRow.classList.add('row');
  topRow.classList.add('div-all-header');
  const topCol = document.createElement('div');
  topCol.classList.add('col');
  topCol.innerHTML = `<h5>${todo.title}</h5>`;
  topRow.appendChild(topCol);
  return topRow;
}

// creates Html elements for a Todo Item : done checkbox and item text
function renderItemData(item) {
  
  const dataDiv = document.createElement('div');
  dataDiv.classList.add('col-sm-7');
  dataDiv.classList.add('col-md-5');
  
  const itemCheckbox = document.createElement('input');
  itemCheckbox.setAttribute('type', 'checkbox');
  const itemId = `check:${item.uuid}`
  itemCheckbox.id = itemId;
  itemCheckbox.classList.add('item-checkbox');
  dataDiv.appendChild(itemCheckbox)
  
  const itemLabel = document.createElement('label');
  itemLabel.id = `label:${item.uuid}`;
  itemLabel.setAttribute('for', itemId);
  itemLabel.classList.add('item-text');
  itemLabel.innerHTML = `${item.text}`;
  dataDiv.appendChild(itemLabel);

  if( item.done === true ) {
    itemCheckbox.setAttribute('checked', 'true');
    itemLabel.classList.add('item-line-through');
  }  

  return dataDiv;
}

// creates Html elements for edit and delete button of Todo Items
function renderItemActionCol(item) {
  
  const actionCol = document.createElement('div');
  actionCol.classList.add('col-sm-1');
  
  const editIcon = document.createElement('a');
  editIcon.href = '#';
  editIcon.classList.add('fa-regular');
  editIcon.classList.add('fa-edit');
  editIcon.classList.add('item-edit-icon');
  editIcon.id = `item:edit:${item.uuid}`;
  actionCol.append(editIcon);

  const delIcon = document.createElement('a');
  delIcon.href = '#';
  delIcon.classList.add('fa-regular');
  delIcon.classList.add('fa-trash-can');
  delIcon.classList.add('item-del-icon');
  delIcon.id = `item:del:${item.uuid}`;
  actionCol.append(delIcon);

  return actionCol;
}

function resetRenderList() {
  const mainContainer = document.getElementById('main-container');
  const initHtml = '<h5>Click on a TODO list to display contents</h5>' +
          '<h5>or create a new one</h5>';
  mainContainer.innerHTML = initHtml;
}

////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Check - Uncheck Item as done /////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

function bindItemCheckActions() {
  document.querySelectorAll('.item-checkbox').forEach( elem => {
    elem.addEventListener('change', processCheckItemEvent);
  });   
}

function processCheckItemEvent(event) {
  const todoItemUuid = event.target.id.split(':')[1];
  const itemText = document.getElementById(`label:${todoItemUuid}`).innerHTML;
  globalData.currentItemUuid = todoItemUuid;
  const itemData = {
    uuid: todoItemUuid,
    listUuid: globalData.currentTodoUuid,
    text: itemText,
    done: event.target.checked
  };
  todosXhr.updateItem(itemData, checkItemCallback);
}

function checkItemCallback(err, data) {
  if(!err) {
    console.log(`Received item from update: ${data}`);
    
    const receivedItem = JSON.parse(data);
    const itemCheckbox = document.getElementById(`check:${receivedItem.uuid}`);
    const itemLabel = document.getElementById(`label:${receivedItem.uuid}`);
    
    // update item in the global data cache
    const cachedTodo = globalData.todos.get(globalData.currentTodoUuid)
            .items.get(receivedItem.uuid);
    cachedTodo.text = receivedItem.text;
    cachedTodo.done = receivedItem.done;

    // update the UI
    if( receivedItem.done === true ) {
      itemCheckbox.setAttribute('checked', 'true');
      itemLabel.classList.add('item-line-through');   
    } else {
      itemCheckbox.setAttribute('checked', 'false');
      itemLabel.classList.remove('item-line-through');   
    }

  } else {
    console.log('Error calling backed service');
  }    
}

////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Edit Item Text ///////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////


function bindItemEditActions() {
  document.querySelectorAll('.item-edit-icon').forEach( elem => {
    elem.addEventListener('click', processEditItemEvent);
  });
}


function processEditItemEvent(event) {
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const itemUuid = targetElem.id.split(':')[2];
  globalData.currentItemUuid = itemUuid;
  const labelEl = document.getElementById(`label:${itemUuid}`);
  const inputTextEdit = document.getElementById('input-text-edit');
  inputTextEdit.value = labelEl.innerHTML;
  
  itemEditModal.show();
}

function bindModalItemEdit() {
  const btnEdit = document.getElementById('btn-text-edit');
  btnEdit.addEventListener('click', editItemText);
}


function editItemText(event) {
  event.preventDefault();
  console.log('editing item text');

  const itemUuid = globalData.currentItemUuid;
  const itemDone = document.getElementById(`check:${itemUuid}`).checked;
  const itemText = document.getElementById('input-text-edit').value;

  const itemData = {
    uuid: itemUuid,
    listUuid: globalData.currentTodoUuid,
    text: itemText,
    done: itemDone
  };

  todosXhr.updateItem(itemData, editItemTextCallback);
  itemEditModal.hide();
}

function editItemTextCallback(err, data) {
  if(!err) {
    
    const receivedItem = JSON.parse(data);
    const itemLabel = document.getElementById(`label:${receivedItem.uuid}`);
    
    // update item in the global data cache
    const cachedTodo = globalData.todos.get(globalData.currentTodoUuid)
            .items.get(receivedItem.uuid);
    cachedTodo.text = receivedItem.text;
    
    itemLabel.innerHTML = receivedItem.text;

  } else {
    console.log('Error calling backed service');
  }    
}

////////////////////////////////////////////////////////////////////////////////////
///////////////////////// Delete Todo Item /////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

function bindItemDeleteActions() {
  document.querySelectorAll('.item-del-icon').forEach( elem => {
    elem.addEventListener('click', processDeleteItemEvent);
  });
}

function processDeleteItemEvent(event) {
  if( confirm('Are you sure you want to delete this TODO list') ) {
    let targetElem = event.target;
    while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
      targetElem = targetElem.parentElement;
    }
    const todoItemUuid = targetElem.id.split(':')[2];
    globalData.currentItemUuid = todoItemUuid;
    todosXhr.deleteItemById(globalData.currentTodoUuid, todoItemUuid, deleteItemCallback);
  }
}

function deleteItemCallback(err, data) {
  if(!err) {
    deleteItemFromCache( globalData.currentTodoUuid, globalData.currentItemUuid );
    renderList( globalData.todos.get(globalData.currentTodoUuid) );
  } else {
    console.log('Error calling backed service');
  }    
}

function deleteItemFromCache(listUuid, itemUuid) {
  const items = globalData.todos.get(listUuid).items;
  items.delete(itemUuid);
}


////////////////////////////////////////////////////////////////////////////////////
//////// Input text and button to add new item to current todo list ////////////////
////////////////////////////////////////////////////////////////////////////////////


function renderNewItem() {
  const divRow = document.createElement('div');
  divRow.classList.add('row');
  divRow.classList.add('div-new-item');
  
  const colInput = document.createElement('div');
  colInput.classList.add('col-md-5');
  divRow.appendChild(colInput);

  const itemInput = document.createElement('input');
  itemInput.type = 'text';
  itemInput.id = 'input-new-item';
  itemInput.name = 'input-new-item';
  itemInput.classList.add('form-control');
  colInput.appendChild(itemInput);  
  
  const colBtn = document.createElement('div');
  colBtn.classList.add('col-md-2');
  divRow.appendChild(colBtn);

  const newItemBtn = document.createElement('button');
  newItemBtn.type = 'button';
  newItemBtn.classList.add('btn');
  newItemBtn.classList.add('btn-primary');
  newItemBtn.innerHTML = 'Add Item';
  colBtn.appendChild(newItemBtn);

  colBtn.addEventListener('click', addNewItem);

  return divRow;

}

function addNewItem() {
  const currentTodoList = globalData.todos.get(globalData.currentTodoUuid);
  // items to add needs to have order index max + 1
  // Iteration over Maps is always in insertion order so items in current TodoList 
  // are already ordered. Last item has the last index
  const itemsArr = Array.from( currentTodoList.items.values() );
  const lastItem = itemsArr[itemsArr.length-1];
  const itemData = {
    listUuid: globalData.currentTodoUuid,
    text: document.getElementById('input-new-item').value,
    orderIdx: lastItem.orderIdx + 1
  }
  console.log(`Adding new todo item: ${JSON.stringify(itemData)}`);
  todosXhr.addItem(itemData, addNewItemCallback);
}

function addNewItemCallback(err, data) {
  if(!err) {
    
    const receivedItem = JSON.parse(data);
    
    // add item to the current Todo list in the global cache
    const currentTodoList = globalData.todos.get(globalData.currentTodoUuid);
    const todoItem = {};
    todoItem.uuid = receivedItem.uuid;
    todoItem.listUuid = receivedItem.listUuid;
    todoItem.text = receivedItem.text;
    todoItem.done = receivedItem.done;
    todoItem.orderIdx = receivedItem.orderIdx;
    todoItem.extraInfo = receivedItem.extraInfo;
    currentTodoList.items.set( todoItem.uuid, todoItem );
    
    renderList(currentTodoList);
    
  } else {
    console.log('Error calling backed service');
  }     
}


export default {
  fetchList,
  renderList,
  resetRenderList,
  bindModalItemEdit
}