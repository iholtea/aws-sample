import todosXhr from './todos-xhr.js'
import globalData from './global-data.js'

function fetchList(listUuid) {
  // this might be just fetchItemsByListId
  // also we should call the fetch only if tha items are not already fetched
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
    todoItem.text = receivedItem.text;
    todoItem.done = receivedItem.done;
    todoItem.extraInfo = receivedItem.extraInfo;
    cachedTodo.items.set( todoItem.uuid, todoItem );
  });
}

function renderList(todo) {
  
  const mainContainer = document.getElementById('main-container');
  mainContainer.innerHTML = '';

  const topRow = document.createElement('div');
  topRow.classList.add('row');
  topRow.classList.add('div-all-header');
  const topCol = document.createElement('div');
  topCol.classList.add('col');
  topCol.innerHTML = `<h5>${todo.title}</h5>`;
  topRow.appendChild(topCol);
  mainContainer.appendChild(topRow);

  todo.items.forEach( (item,idx) => {
    
    const rowDiv = document.createElement('div');
    rowDiv.classList.add('row');

    const dataDiv = document.createElement('div');
    rowDiv.appendChild(dataDiv);
    dataDiv.classList.add('col-sm-7');
    dataDiv.classList.add('col-md-5');
    
    const itemCheckbox = document.createElement('input');
    itemCheckbox.setAttribute('type', 'checkbox');
    const itemId = `check:${item.uuid}`
    itemCheckbox.id = itemId;
    itemCheckbox.id = itemId;
    itemCheckbox.classList.add('item-checkbox');
    dataDiv.appendChild(itemCheckbox)
    
    const itemLabel = document.createElement('label');
    itemLabel.setAttribute('for', itemId);
    itemLabel.classList.add('item-text');
    itemLabel.innerHTML = `${item.text}`;
    dataDiv.appendChild(itemLabel);

    if( item.done === true ) {
      itemCheckbox.setAttribute('checked', 'true');
      itemLabel.classList.add('item-line-through');
    }

    const delCol = document.createElement('div');
    delCol.classList.add('col-sm-1');
    rowDiv.appendChild(delCol);

    const delIcon = document.createElement('a');
    delIcon.href = '#';
    delIcon.classList.add('fa-regular');
    delIcon.classList.add('fa-trash-can');
    delIcon.classList.add('item-del-icon');
    delIcon.id = `item:del:${item.uuid}`;
    delCol.append(delIcon);

    mainContainer.appendChild(rowDiv);
  
  });

  bindItemDeleteActions();
  
}

function bindItemDeleteActions() {
  document.querySelectorAll('.item-del-icon').forEach( elem => {
    elem.addEventListener('click', processDeleteItemEvent);
  });
}

function processDeleteItemEvent(event) {
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoItemUuid = targetElem.id.split(':')[2];
  globalData.currentItemUuid = todoItemUuid;
  console.log( `clicked delete item for id: ${todoItemUuid}` );
  todosXhr.deleteItemById(globalData.currentTodoUuid, todoItemUuid, deleteItemCallback);
  
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

function resetRenderList() {
  const mainContainer = document.getElementById('main-container');
  const initHtml = '<h5>Click on a TODO list to display contents</h5>' +
          '<h5>or create a new one</h5>';
  mainContainer.innerHTML = initHtml;
}

export default {
  fetchList,
  renderList,
  resetRenderList
}