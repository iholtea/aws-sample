const baseUrl = 'http://localhost:8080/todos';
let todos;

function  getAllLists(callback) {
  const xhr = new XMLHttpRequest();
  xhr.open('GET', baseUrl, true);
  xhr.onload = function() {
    if(xhr.status === 200) {
      // TODO - responseText should not be empty
      // I get empty when an exception happens on the the server side
      // this is the default stuff Jersey does probably.
      // should handle it and send some kind of error message
      callback(null, xhr.responseText);
    } else {
        callback('ERR_LOAD', xhr.status);
    }
  }
  xhr.onerror = function() {
      callback( 'ERR_OTHER', null );
  }
  xhr.send();
}

/*
function displayCustomers() {
  customerXhr.getCustomers(getCustomersCallback);
}

function getCustomersCallback(err,data) {
  if(!err) {
    customerUI.displayCustomerList(JSON.parse(data),10);
    bindActions();
  } else if( err === 'ERR_LOAD') {
    const custContainer = document.getElementById('customer-container');
    if(data === 404) {
      custContainer.innerHTML = `
        <h6>Eror 404</h6>
        Resource not found on endpoint API
      `;
    } else {
      custContainer.innerHTML = `
        <h6>Eror within the response from customer API</h6>
      `;
    }
  } else if( err === 'ERR_OTHER') {
    const custContainer = document.getElementById('customer-container');
    custContainer.innerHTML = `
      <h6>General error.</h6> 
      Check if customer API is reachable</h6>
    `;
  }
}
*/     

function getAllListsCallback(err, data) {
  if(!err) {
    console.log(data);
    todos = JSON.parse(data);
    displayAllLists();
    bindListsDisplayActions();
  } else {
    console.log('Error calling backed service');
  }
}

function displayAllLists() {
  const todosContainer = document.getElementById('all-lists-container');
  const ul = document.createElement('ul');
  
  todos.forEach( (todoList, todoListIdx) => {
    
    // compute how many items are done
    const doneNr = todoList.items.reduce( (result, item) => {
      return item.done ? result+1 : result;
    }, 0);
    
    const li = document.createElement('li');
    const liLink = document.createElement('a');
    liLink.id = `todo-list-${todoListIdx}`;
    liLink.href = '#';
    liLink.className = 'todo-list-display';
    liLink.innerHTML = `${todoList.title} ${doneNr}/${todoList.items.length}`;
    li.appendChild(liLink);
    
    ul.appendChild(li);
  });
  todosContainer.appendChild(ul);
}

function bindListsDisplayActions() {
  document.querySelectorAll('.todo-list-display').forEach( elem => {
    elem.addEventListener('click', displayList);
  });
}

function displayList(event) {
  
  // get the index of the TodoList that was clicked(selected)
  let targetElem = event.target;
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListIdx = targetElem.id.split('-')[2];
  const todo = todos[todoListIdx];
  
  const mainContainer = document.getElementById('main-container');
  mainContainer.innerHTML = '';

  const fieldset = document.createElement('fieldset');
  mainContainer.appendChild(fieldset);

  const legend = document.createElement('legend');
  legend.innerHTML = `${todo.title}`;
  fieldset.appendChild(legend);
 
  todo.items.forEach( (item,idx) => {
    
    const itemDiv = document.createElement('div');
    itemDiv.classList.add('item-div');

    const itemId = `check-${idx+1}`;
    
    const itemCheckbox = document.createElement('input');
    itemCheckbox.setAttribute('type', 'checkbox');
    itemCheckbox.name = itemId;
    itemCheckbox.id = itemId;
    
    const itemLabel = document.createElement('label');
    itemLabel.setAttribute('for', itemId);
    itemLabel.classList.add('item-text');
    itemLabel.innerHTML = `${item.text}`;

    if( item.done === true ) {
      itemCheckbox.setAttribute('checked', 'true');
      itemLabel.classList.add('item-line-through');
    }

    itemDiv.appendChild(itemCheckbox);
    itemDiv.appendChild(itemLabel);

    fieldset.appendChild(itemDiv);
   
  });

}

bindListsDisplayActions();

getAllLists( getAllListsCallback );
