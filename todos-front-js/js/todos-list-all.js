import todosXhr from './todos-xhr.js'
import globalData from './global-data.js'
import todosDetail from './todos-list-detail.js'
import todosAdd from './todos-list-add.js'

function fetchAllLists() {
  todosXhr.fetchAllLists( fetchAllListsCallback );
}

function fetchAllListsCallback(err, data, status) {
  if(!err) {
    parseAllLists(data)
    renderAllLists(globalData.todos);
  } else if( err === 'ERR_HTTP_STATUS' ) {
    displayAllListsMessage(`Http Status ${status} received`);    
  } else if( err === 'ERR_OTHER' ) {
    displayAllListsMessage('Cannot fetch TODO lists');
  }
}

// obj['state'] = 'Mumbai' - this modifies the existing state property of obj
// obj.state = 'Mumbai' - this should add the state property ot obj.
// rename this to parseFetchedAllLists
function parseAllLists(receivedData) {
  const receivedTodos = JSON.parse(receivedData);
  receivedTodos.forEach( (receivedTodo) => {
    const todo = {};
    todo.uuid = receivedTodo.uuid;
    todo.title = receivedTodo.title;
    todo.creationDate = receivedTodo.creationDate;
    todo.lastUpdate = receivedTodo.lastUpdate;
    todo.extraInfo = receivedTodo.extraInfo;
    todo.items = new Map();
    globalData.todos.set(todo.uuid,todo);
  });
}

function renderAllLists(todos) {
  
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  
  renderHeader(todosContainer);

  todos.forEach( (todoList, key) => {
    
    const rowDiv = document.createElement('div');
    rowDiv.classList.add('row');
    
    const textColDiv = document.createElement('div');
    textColDiv.classList.add('col');

    const iconStar = document.createElement('i');
    iconStar.classList.add('fa-solid');
    iconStar.classList.add('fa-splotch');
    textColDiv.appendChild(iconStar);

    const linkEl = document.createElement('a');
    linkEl.id = `list:disp:${todoList.uuid}`;
    linkEl.href = '#';
    linkEl.classList.add('todo-list-display');
    linkEl.innerHTML = `${todoList.title}`;
    textColDiv.appendChild(linkEl);
    
    rowDiv.appendChild(textColDiv);

    const delColDiv = document.createElement('div');
    delColDiv.classList.add('col');

    const delIcon = document.createElement('a');
    delIcon.href = '#';
    delIcon.classList.add('fa-regular');
    delIcon.classList.add('fa-trash-can');
    delIcon.classList.add('list-del-icon');
    delIcon.id = `list:del:${todoList.uuid}`;
    
    delColDiv.appendChild(delIcon);

    rowDiv.appendChild(delColDiv);

    todosContainer.appendChild(rowDiv);
    
  });
  
  bindListsRenderActions();
  bindListsDeleteActions();

  renderNewBtn(todosContainer);
}

function bindListsRenderActions() {
  document.querySelectorAll('.todo-list-display').forEach( elem => {
    elem.addEventListener('click', processRenderListEvent);
  });
}


/*
* List of todo-s is made of elements like
* <a id="list:disp:${todoList.uuid}" class="todo-list-display">Todo Title</a></li>
*
* When click-ing on a todo list -> display it and its elements
*/
function processRenderListEvent(event) {
  
  // get the uuid of the TodoList that was clicked(selected)
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListUuid = targetElem.id.split(':')[2];
  
  // set current TodoList to the selected one
  globalData.currentTodoUuid = todoListUuid;
  
  todosDetail.fetchList(todoListUuid);
  
}

function bindListsDeleteActions() {
  document.querySelectorAll('.list-del-icon').forEach( elem => {
    elem.addEventListener('click', processDeleteListEvent);
  }); 
}

function processDeleteListEvent(event) {
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListUuid = targetElem.id.split(':')[2];
  console.log( `TodoList to delete: ${todoListUuid}` );  
}

function displayAllListsMessage(message) {
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  renderHeader(todosContainer);

  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');
  colDiv.innerHTML = `<h5>${message}</h5>`;
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);

  renderNewBtn(todosContainer);
}


function renderHeader(todosContainer) {
  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  rowDiv.classList.add('div-all-header');
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');
  colDiv.innerHTML='<h4>TODO lists</h4>';
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);
}


function renderNewBtn(todosContainer) {
  
  const rowDiv = document.createElement('div');
  rowDiv.classList.add('row');
  rowDiv.classList.add('div-new-list-btn');
  
  const colDiv = document.createElement('div');
  colDiv.classList.add('col');

  const newBtn = document.createElement('button');
  newBtn.type = 'button';
  newBtn.classList.add('btn');
  newBtn.classList.add('btn-primary');
  newBtn.innerHTML = 'New TODO list';
  newBtn.addEventListener('click', todosAdd.renderNewListForm);

  colDiv.appendChild(newBtn);
  rowDiv.appendChild(colDiv);
  todosContainer.appendChild(rowDiv);

}

export default {
  fetchAllLists,
  renderAllLists
}