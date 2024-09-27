#include "undo_redo_helper.h"

void gpupixel::UndoRedoHelper::addRecord(const gpupixel::OpenPSRecord &record) {
  int lastIndex = recordList.size() - 1;
  if (currentIndex < lastIndex) {
    for (int i = lastIndex + 1; i < lastIndex; i++) {
      recordList.pop_back();
    }
  }
  recordList.emplace_back(record);
  currentIndex++;
  Util::Log("UndoRedoHelper", "addRecord {%s}", record.toString().c_str());
  Util::Log("UndoRedoHelper", "currentIndex: %d", currentIndex);
}
