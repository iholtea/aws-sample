const todos = [

  {
    "uuid": "uuid-list-01",
    "title": "Norway road trip",
    "items": [
      {
        "uuid": "uuid-item-01-01",
        "text": "Buy travel insurance",
        "done": false
      },
      {
        "uuid": "uuid-item-01-02",
        "text": "Create online account for Norway road tax",
        "done": true
      },
      {
        "uuid": "uuid-item-01-03",
        "text": "Ferry reservation DK-NO",
        "done": false
      }
    ]
  },

  {
    "uuid": "uuid-list-02",
    "title": "Supermarket shopping",
    "items": [
      {
        "uuid": "uuid-item-02-01",
        "text": "Oranges 2kg",
        "done": false
      },
      {
        "uuid": "uuid-item-02-02",
        "text": "Pink lady apples 2 packs",
        "done": true
      },
      {
        "uuid": "uuid-item-02-03",
        "text": "Potatoes 3kg",
        "done": true
      },
      {
        "uuid": "uuid-item-02-03",
        "text": "Orange juice 2 boxes",
        "done": false
      }
    ]
  }

];

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
    liLink.href = `#${liLink.id}`;
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
  console.log(targetElem);
  while( !(targetElem.nodeName.toUpperCase() === 'A') ) {
    targetElem = targetElem.parentElement;
  }
  const todoListIdx = targetElem.id.split('-')[2];
  console.log(`selected idx: ${todoListIdx}`);

}

displayAllLists();
bindListsDisplayActions();
