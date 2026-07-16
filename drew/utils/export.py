import pandas as pd
from datetime import datetime

def export_to_csv(db_manager, file_path):
    """
    Export database odds/events to a CSV file using Pandas.
    """
    conn = db_manager.get_connection()

    query = '''
        SELECT e.home_team, e.away_team, e.competition, e.commence_time,
               o.market_key, o.selection_name, o.price, o.confidence_score, o.risk_flags
        FROM odds o
        JOIN events e ON o.event_id = e.id
    '''

    df = pd.read_sql_query(query, conn)
    conn.close()

    df['exported_at'] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    df.to_csv(file_path, index=False, encoding='utf-8')
