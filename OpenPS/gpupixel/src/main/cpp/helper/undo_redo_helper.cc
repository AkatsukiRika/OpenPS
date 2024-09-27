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

bool gpupixel::UndoRedoHelper::canUndo() {
  int recordCount = recordList.size();
  if (recordCount == 0) {
    return false;
  }
  return currentIndex > 0;
}

bool gpupixel::UndoRedoHelper::canRedo() {
  int recordCount = recordList.size();
  int lastIndex = recordCount - 1;
  if (recordCount == 0) {
    return false;
  }
  return currentIndex < lastIndex;
}

void gpupixel::UndoRedoHelper::addEmptyRecord() {
  auto emptyRecord = OpenPSRecord(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
  addRecord(emptyRecord);
}

gpupixel::UndoRedoHelper::UndoRedoHelper() {
  addEmptyRecord();
}
