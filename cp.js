let refreshDBButton;

window.addEventListener('load', ()=>{
    refreshDBButton = document.getElementById('refresh_btn');
    refreshDBButton.addEventListener('click', ()=>{refreshDB();});
});

async function refreshDB() {
    refreshDBButton.querySelector('.btn_text').innerText = 'Refreshing...';
    refreshDBButton.querySelector('.loading_spinner').style.display = 'block';

    res = await fetch('http://localhost:8000/reload_db');
    if(res.ok) {
        refreshDBButton.querySelector('.btn_text').innerText = 'Refresh image database';
        refreshDBButton.querySelector('.loading_spinner').style.display = 'none';
    } else {
        refreshDBButton.querySelector('.btn_text').innerText = 'Refresh failed';
        refreshDBButton.querySelector('.loading_spinner').style.display = 'none';
    }
}