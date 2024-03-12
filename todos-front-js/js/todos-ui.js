function displayList(todo) {
  
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

function displayAllLists(todos) {
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = '';
  const ul = document.createElement('ul');
  
  todos.forEach( (todoList, todoListIdx) => {
    
    // compute how many items are done
    /*
    const doneNr = todoList.items.reduce( (result, item) => {
      return item.done ? result+1 : result;
    }, 0);
    */
    
    const li = document.createElement('li');
    const liLink = document.createElement('a');
    liLink.id = `todo:list:${todoList.uuid}`;
    liLink.href = '#';
    liLink.className = 'todo-list-display';
    liLink.innerHTML = `${todoList.title}`;
    li.appendChild(liLink);
    
    ul.appendChild(li);
  });
  todosContainer.appendChild(ul);
}

function displayAllListsMessage(message) {
  const todosContainer = document.getElementById('all-lists-container');
  todosContainer.innerHTML = `<h5>${message}</h5>`;
}

function displayNewListForm() {
  console.log('display new list form');
}

export default {
  displayList,
  displayAllLists,
  displayAllListsMessage,
  displayNewListForm  
}
